from django.shortcuts import render, redirect
from django.http import HttpResponse, JsonResponse
from django.views.decorators.csrf import csrf_exempt
import os
import json
import time
from datetime import datetime, timedelta

# Spotify / external deps
import spotipy
from spotipy.oauth2 import SpotifyOAuth
from dotenv import load_dotenv
from serpapi import GoogleSearch

from .utils import (
    get_cached_bookmarks,
    save_bookmark,
    remove_bookmark,
    toggle_bookmark,
    search_original_lyrics,
)
from .models import BookmarkedSong, SongLyrics
from .gemini_utils import (
    process_bookmarked_page_for_lyrics,
    test_gemini_connection,
    generate_ai_translation_and_romanization,
)
from django.core.paginator import Paginator
from django.db.models import Q


# Load environment variables
load_dotenv()

# Spotify credentials
SPOTIFY_CLIENT_ID = os.getenv("SPOTIFY_CLIENT_ID")
SPOTIFY_CLIENT_SECRET = os.getenv("SPOTIFY_CLIENT_SECRET")
SPOTIFY_REDIRECT_URI = os.getenv("SPOTIFY_REDIRECT_URI", "http://127.0.0.1:8000/spotify/callback")

# SerpAPI credentials
SERPAPI_KEY = os.getenv("SERPAPI_KEY")

# Initialize Spotify client
if SPOTIFY_CLIENT_ID and SPOTIFY_CLIENT_SECRET and SPOTIFY_REDIRECT_URI:
    sp_oauth = SpotifyOAuth(
        client_id=SPOTIFY_CLIENT_ID,
        client_secret=SPOTIFY_CLIENT_SECRET,
        redirect_uri=SPOTIFY_REDIRECT_URI,
        scope="user-read-currently-playing user-read-playback-state",
    )
else:
    sp_oauth = None


def search_lyrics_translations(song_name, artist_name):
    """Search for lyrics translations using SerpAPI"""
    if not SERPAPI_KEY:
        return [], {}, False

    bookmarks = get_cached_bookmarks(song_name, artist_name)
    if bookmarks:
        bookmarked_matches = []
        for url, meta in bookmarks.items():
            if isinstance(meta, dict) or meta is True:
                title = None
                if isinstance(meta, dict):
                    title = meta.get("title")
                if not title:
                    try:
                        title = url.split("/")[-1].replace("-", " ").title() or url
                    except Exception:
                        title = url
                bookmarked_matches.append(
                    {
                        "url": url,
                        "title": title,
                    }
                )
        if bookmarked_matches:
            return bookmarked_matches, bookmarks, True

    search_query = f"{song_name} {artist_name} lyrics translation"

    params = {
        "engine": "google",
        "q": search_query,
        "api_key": SERPAPI_KEY,
        "num": 10,
        "gl": "in",
        "hl": "en",
    }

    try:
        time.sleep(2)
        search = GoogleSearch(params)
        results = search.get_dict()

        if "error" in results or "organic_results" not in results:
            print("No results found")
            return [], {}, False

        matches = []
        for result in results["organic_results"][:10]:
            url = result.get("link", "")
            title = result.get("title", "") or url.split("/")[-1].replace("-", " ").title()

            matches.append(
                {
                    "url": url,
                    "title": title,
                }
            )

        print(f"Found {len(matches)} results using SerpAPI")
        return matches, {}, False

    except Exception as e:
        print(f"Search error: {str(e)}")
        return [], {}, False


def is_token_expired(expires_at):
    """Check if the token has expired"""
    if not expires_at:
        return True
    return datetime.now().timestamp() > (float(expires_at) - 60)


def spotify_view(request):
    """Main Spotify view - renders the index page"""
    return render(request, "spotify/index.html")


def spotify_login(request):
    """Redirect to Spotify authorization page"""
    if not sp_oauth:
        return HttpResponse("Spotify credentials not configured", status=500)
    auth_url = sp_oauth.get_authorize_url()
    return redirect(auth_url)


def spotify_callback(request):
    """Handle Spotify authorization callback"""
    if not sp_oauth:
        return HttpResponse("Spotify credentials not configured", status=500)

    code = request.GET.get("code")
    if not code:
        return HttpResponse("Authorization failed: No code provided", status=400)

    try:
        token_info = sp_oauth.get_access_token(code)
        if not token_info:
            return HttpResponse("Failed to get access token", status=400)

        expires_at = datetime.now() + timedelta(seconds=token_info["expires_in"])
        token_info["expires_at"] = expires_at.timestamp()

        return HttpResponse(
            f"""
            <html>
                <body>
                    <script>
                        localStorage.setItem('spotify_token', '{token_info["access_token"]}');
                        localStorage.setItem('spotify_token_expires_at', '{token_info["expires_at"]}');
                        window.location.href = '/spotify/';
                    </script>
                </body>
            </html>
        """
        )
    except Exception as e:
        return HttpResponse(f"Authorization error: {str(e)}", status=400)


@csrf_exempt
def spotify_toggle_bookmark(request):
    """Handle toggling bookmark for a search result"""
    if request.method != "POST":
        return JsonResponse({"success": False, "error": "Method not allowed"}, status=405)

    try:
        data = json.loads(request.body)
        song_name = data.get("song_name")
        artist_name = data.get("artist_name")
        url = data.get("url")
        title = data.get("title")
        extract_lyrics = data.get("extract_lyrics", True)

        if not all([song_name, artist_name, url]):
            return JsonResponse({"success": False, "error": "Missing required parameters"})

        success, is_bookmarked = toggle_bookmark(song_name, artist_name, url, title)

        if success and is_bookmarked and extract_lyrics:
            try:
                lyrics_result = process_bookmarked_page_for_lyrics(url, song_name, artist_name)

                if lyrics_result.get("success", False):
                    SongLyrics.save_lyrics(
                        song_name=song_name,
                        artist_name=artist_name,
                        original_lyrics=lyrics_result.get("original_lyrics", ""),
                        translated_lyrics=lyrics_result.get("translated_lyrics", ""),
                        source_url=url,
                    )

                    return JsonResponse(
                        {
                            "success": True,
                            "bookmarked": is_bookmarked,
                            "lyrics_extracted": True,
                            "lyrics_data": {
                                "has_original": bool(lyrics_result.get("original_lyrics")),
                                "has_translation": bool(lyrics_result.get("translated_lyrics")),
                            },
                        }
                    )
                else:
                    return JsonResponse(
                        {
                            "success": True,
                            "bookmarked": is_bookmarked,
                            "lyrics_extracted": False,
                            "lyrics_error": lyrics_result.get("error", "Unknown error"),
                        }
                    )

            except Exception as lyrics_error:
                print(f"Lyrics extraction error: {lyrics_error}")
                return JsonResponse(
                    {
                        "success": True,
                        "bookmarked": is_bookmarked,
                        "lyrics_extracted": False,
                        "lyrics_error": str(lyrics_error),
                    }
                )

        if success:
            return JsonResponse({"success": True, "bookmarked": is_bookmarked})
        else:
            return JsonResponse({"success": False, "error": "Failed to toggle bookmark"})

    except Exception as e:
        return JsonResponse({"success": False, "error": str(e)})


def spotify_current_song(request):
    """Get current playing song and lyrics translations"""
    token = request.GET.get("token")
    expires_at = request.GET.get("expires_at")

    if not token:
        return JsonResponse(
            {
                "error": "No token provided",
                "message": "Please log in again",
                "requires_login": True,
                "redirect_url": "/spotify/login",
            }
        )

    try:
        if is_token_expired(expires_at):
            print("Token expired, redirecting to login")
            return JsonResponse(
                {
                    "error": "Token expired",
                    "message": "Please log in again",
                    "requires_login": True,
                    "redirect_url": "/spotify/login",
                }
            )

        sp = spotipy.Spotify(auth=token)

        try:
            current = sp.current_playback()
            if current and current["item"]:
                track = current["item"]
                song_name = track["name"]
                artist_name = track["artists"][0]["name"]

                existing_lyrics = SongLyrics.get_lyrics(song_name, artist_name)
                if existing_lyrics and existing_lyrics.has_lyrics():
                    return JsonResponse(
                        {
                            "song": song_name,
                            "artist": artist_name,
                            "has_lyrics": True,
                            "lyrics_data": {
                                "original_lyrics": existing_lyrics.original_lyrics,
                                "translated_lyrics": existing_lyrics.translated_lyrics,
                                "source_url": existing_lyrics.source_url,
                            },
                            "show_custom_page": True,
                        }
                    )

                results, bookmarks, is_cached = search_lyrics_translations(song_name, artist_name)

                return JsonResponse(
                    {
                        "song": song_name,
                        "artist": artist_name,
                        "has_lyrics": False,
                        "lyrics_sources": results,
                        "bookmarks": bookmarks,
                        "is_cached": is_cached,
                        "show_custom_page": False,
                    }
                )
            return JsonResponse({"error": "No song currently playing"})

        except spotipy.SpotifyException as e:
            if e.http_status == 401:
                print("Token invalid, redirecting to login")
                return JsonResponse(
                    {
                        "error": "Invalid token",
                        "message": "Please log in again",
                        "requires_login": True,
                        "redirect_url": "/spotify/login",
                    }
                )
            else:
                print(f"Spotify API error: {str(e)}")
                return JsonResponse({"error": "Spotify API error", "message": str(e)})

    except Exception as e:
        print(f"Unexpected error: {str(e)}")
        return JsonResponse({"error": "Unexpected error", "message": str(e)})


def spotify_lyrics_page(request):
    """Display custom lyrics page for songs with extracted lyrics"""
    song_name = request.GET.get("song")
    artist_name = request.GET.get("artist")

    if not song_name or not artist_name:
        return HttpResponse("Missing song or artist parameter", status=400)

    lyrics = SongLyrics.get_lyrics(song_name, artist_name)
    if not lyrics or not lyrics.has_lyrics():
        return HttpResponse("No lyrics found for this song", status=404)

    context = {
        "song_name": song_name,
        "artist_name": artist_name,
        "lyrics": lyrics,
        "has_original": bool(lyrics.original_lyrics),
        "has_translation": bool(lyrics.translated_lyrics),
        "bookmark_id": lyrics.bookmark.id if lyrics and lyrics.bookmark else None,
    }

    return render(request, "spotify/lyrics_page.html", context)


@csrf_exempt
def spotify_extract_lyrics(request):
    """Manual lyrics extraction for a bookmarked song"""
    if request.method != "POST":
        return JsonResponse({"success": False, "error": "Method not allowed"}, status=405)

    try:
        data = json.loads(request.body)
        song_name = data.get("song_name")
        artist_name = data.get("artist_name")
        url = data.get("url")
        force_reextract = data.get("force_reextract", False)

        if not all([song_name, artist_name, url]):
            return JsonResponse({"success": False, "error": "Missing required parameters"})

        existing_lyrics = SongLyrics.get_lyrics(song_name, artist_name)
        if existing_lyrics and existing_lyrics.has_lyrics() and force_reextract:
            existing_lyrics.delete()
        elif existing_lyrics and existing_lyrics.has_lyrics() and not force_reextract:
            return JsonResponse(
                {"success": False, "error": "Lyrics already exist. Use force_reextract to overwrite."}
            )
        elif existing_lyrics and not existing_lyrics.has_lyrics():
            existing_lyrics.delete()

        lyrics_result = process_bookmarked_page_for_lyrics(url, song_name, artist_name)

        if lyrics_result.get("success", False):
            lyrics_record, created = SongLyrics.save_lyrics(
                song_name=song_name,
                artist_name=artist_name,
                original_lyrics=lyrics_result.get("original_lyrics", ""),
                translated_lyrics=lyrics_result.get("translated_lyrics", ""),
                source_url=url,
            )

            return JsonResponse(
                {
                    "success": True,
                    "created": created,
                    "reextracted": force_reextract,
                    "lyrics_data": {
                        "has_original": bool(lyrics_result.get("original_lyrics")),
                        "has_translation": bool(lyrics_result.get("translated_lyrics")),
                        "source_language": lyrics_result.get("source_language"),
                        "target_language": lyrics_result.get("target_language"),
                    },
                }
            )
        else:
            return JsonResponse(
                {
                    "success": False,
                    "error": lyrics_result.get("error", "Unknown error"),
                    "extraction_notes": lyrics_result.get("extraction_notes", ""),
                }
            )

    except Exception as e:
        return JsonResponse({"success": False, "error": str(e)})


@csrf_exempt
def spotify_test_gemini(request):
    """Test Gemini API connection"""
    if request.method != "GET":
        return JsonResponse({"success": False, "error": "Method not allowed"}, status=405)

    success, message = test_gemini_connection()
    return JsonResponse({"success": success, "message": message})


@csrf_exempt
def spotify_ai_translate_search(request):
    """Search for lyrics sources using SERP API for AI translation on lyrics page"""
    if request.method != "POST":
        return JsonResponse({"success": False, "error": "Method not allowed"}, status=405)

    if not SERPAPI_KEY:
        return JsonResponse({"success": False, "error": "SERP API key not configured"}, status=400)

    try:
        data = json.loads(request.body)
        song_name = data.get("song_name")
        artist_name = data.get("artist_name")

        if not all([song_name, artist_name]):
            return JsonResponse({"success": False, "error": "Missing required parameters"})

        search_query = f"{song_name} {artist_name} lyrics"

        params = {
            "engine": "google",
            "q": search_query,
            "api_key": SERPAPI_KEY,
            "num": 10,
            "gl": "in",
            "hl": "en",
        }

        try:
            time.sleep(1)
            search = GoogleSearch(params)
            results = search.get_dict()

            if "error" in results or "organic_results" not in results:
                return JsonResponse({"success": False, "error": "No search results found"})

            sources = []
            for result in results["organic_results"][:10]:
                url = result.get("link", "")
                title = result.get("title", "") or url.split("/")[-1].replace("-", " ").title()
                snippet = result.get("snippet", "")

                sources.append({"url": url, "title": title, "snippet": snippet})

            return JsonResponse(
                {
                    "success": True,
                    "sources": sources,
                    "query": search_query,
                }
            )

        except Exception as e:
            return JsonResponse({"success": False, "error": f"Search failed: {str(e)}"})

    except Exception as e:
        return JsonResponse({"success": False, "error": str(e)})


@csrf_exempt
def spotify_extract_and_translate(request):
    """Extract lyrics from selected URL and generate AI translation/romanization"""
    if request.method != "POST":
        return JsonResponse({"success": False, "error": "Method not allowed"}, status=405)

    try:
        data = json.loads(request.body)
        song_name = data.get("song_name")
        artist_name = data.get("artist_name")
        selected_url = data.get("selected_url") or data.get("source_url")

        if not all([song_name, artist_name, selected_url]):
            return JsonResponse({"success": False, "error": "Missing required parameters"})

        from .gemini_utils import (
            extract_page_content,
            clean_html_for_gemini,
            extract_lyrics_with_gemini,
            generate_ai_translation_and_romanization,
        )

        html_content = extract_page_content(selected_url)
        if not html_content:
            return JsonResponse({"success": False, "error": "Failed to fetch content from the selected URL"})

        clean_content = clean_html_for_gemini(html_content)
        if not clean_content:
            return JsonResponse({"success": False, "error": "Failed to extract readable content from the page"})

        extraction_result = extract_lyrics_with_gemini(clean_content, song_name, artist_name)

        if not extraction_result.get("success"):
            return JsonResponse(
                {"success": False, "error": extraction_result.get("error", "Failed to extract lyrics")}
            )

        original_lyrics = extraction_result.get("original_lyrics", "")
        if not original_lyrics:
            return JsonResponse({"success": False, "error": "No lyrics found in the extracted content"})

        ai_result = generate_ai_translation_and_romanization(original_lyrics, song_name, artist_name)

        lyrics, created = SongLyrics.save_lyrics(
            song_name=song_name,
            artist_name=artist_name,
            original_lyrics=original_lyrics,
            translated_lyrics=ai_result.get("word_to_word_translation", "") if ai_result.get("success") else "",
            source_url=selected_url,
        )

        if ai_result.get("success"):
            lyrics.ai_romanized = ai_result.get("romanized_lyrics", "")
            lyrics.ai_translation = ai_result.get("word_to_word_translation", "")
            lyrics.save()

        return JsonResponse(
            {
                "success": True,
                "message": "Lyrics extracted and translated successfully",
                "data": {
                    "has_original": bool(original_lyrics),
                    "has_translation": ai_result.get("success") and bool(ai_result.get("word_to_word_translation")),
                    "has_romanization": ai_result.get("success") and bool(ai_result.get("romanized_lyrics")),
                    "detected_language": ai_result.get("detected_language") if ai_result.get("success") else None,
                    "confidence_score": ai_result.get("confidence_score", 0.0) if ai_result.get("success") else 0.0,
                },
            }
        )

    except Exception as e:
        return JsonResponse({"success": False, "error": str(e)})


@csrf_exempt
def spotify_ai_translate(request):
    """AI translation and romanization for a song from the manage/bookmarking page"""
    if request.method != "POST":
        return JsonResponse({"success": False, "error": "Method not allowed"}, status=405)

    try:
        data = json.loads(request.body)
        bookmark_id = data.get("bookmark_id")
        song_name = data.get("song_name")
        artist_name = data.get("artist_name")

        if not all([bookmark_id, song_name, artist_name]):
            return JsonResponse({"success": False, "error": "Missing required parameters"})

        try:
            bookmark = BookmarkedSong.objects.get(id=bookmark_id)
        except BookmarkedSong.DoesNotExist:
            return JsonResponse({"success": False, "error": "Bookmark not found"})

        lyrics = SongLyrics.get_lyrics(song_name, artist_name)
        if not lyrics or not lyrics.original_lyrics:
            lyrics_search_result = search_original_lyrics(song_name, artist_name)
            if "error" in lyrics_search_result:
                return JsonResponse({"success": False, "error": lyrics_search_result["error"]})

            selected_url = None
            if lyrics_search_result.get("urls"):
                selected_url = lyrics_search_result["urls"][0]
            elif lyrics_search_result.get("url"):
                selected_url = lyrics_search_result["url"]

            if not selected_url:
                return JsonResponse({"success": False, "error": "No lyrics sources found"})

            from .gemini_utils import (
                extract_page_content,
                clean_html_for_gemini,
                extract_lyrics_with_gemini,
                generate_ai_translation_and_romanization,
            )

            html_content = extract_page_content(selected_url)
            if not html_content:
                return JsonResponse({"success": False, "error": "Failed to extract content from URL"})

            cleaned_content = clean_html_for_gemini(html_content)
            extraction_result = extract_lyrics_with_gemini(cleaned_content, song_name, artist_name)

            if extraction_result.get("success"):
                original_lyrics = extraction_result.get("original_lyrics", "")
                if not original_lyrics:
                    return JsonResponse({"success": False, "error": "No lyrics found in the extracted content"})

                lyrics, _ = SongLyrics.save_lyrics(
                    song_name=song_name,
                    artist_name=artist_name,
                    original_lyrics=original_lyrics,
                    translated_lyrics="",
                    source_url=selected_url,
                )
            else:
                return JsonResponse(
                    {
                        "success": False,
                        "error": "Failed to extract lyrics: " + extraction_result.get("error", "Unknown error"),
                    }
                )

        if lyrics and lyrics.original_lyrics:
            ai_result = generate_ai_translation_and_romanization(
                lyrics.original_lyrics,
                song_name=song_name,
                artist_name=artist_name,
            )

            if ai_result.get("success"):
                lyrics.ai_romanized = ai_result.get("romanized_lyrics", "")
                lyrics.ai_translation = ai_result.get("word_to_word_translation", "")
                lyrics.save()

                return JsonResponse(
                    {
                        "success": True,
                        "message": "AI translation generated successfully",
                        "data": {
                            "has_original": True,
                            "has_translation": bool(lyrics.ai_translation),
                            "has_romanization": bool(lyrics.ai_romanized),
                            "detected_language": ai_result.get("detected_language"),
                            "confidence_score": ai_result.get("confidence_score", 0.0),
                        },
                    }
                )
            else:
                return JsonResponse(
                    {
                        "success": False,
                        "error": "Failed to generate AI translation: " + ai_result.get("error", "Unknown error"),
                    }
                )
        else:
            return JsonResponse({"success": False, "error": "No lyrics available for translation"})

    except Exception as e:
        return JsonResponse({"success": False, "error": str(e)})


def spotify_manage_songs(request):
    """Management page showing all bookmarked songs with search and delete functionality"""
    search_query = request.GET.get("search", "").strip()

    bookmarks = BookmarkedSong.objects.all().order_by("song_name")

    if search_query:
        bookmarks = bookmarks.filter(
            Q(song_name__icontains=search_query)
            | Q(artist_name__icontains=search_query)
            | Q(title__icontains=search_query)
        )

    songs_data = []
    for bookmark in bookmarks:
        lyrics = SongLyrics.get_lyrics(bookmark.song_name, bookmark.artist_name)
        songs_data.append(
            {
                "bookmark": bookmark,
                "lyrics": lyrics,
                "has_lyrics": lyrics.has_lyrics() if lyrics else False,
            }
        )

    def calculate_db_space():
        total_size = 0
        for bookmark in BookmarkedSong.objects.all():
            total_size += len(str(bookmark.song_name) or "")
            total_size += len(str(bookmark.artist_name) or "")
            total_size += len(str(bookmark.bookmarked_url) or "")
            total_size += len(str(bookmark.title) or "")

        for lyrics in SongLyrics.objects.all():
            total_size += len(str(lyrics.original_lyrics) or "")
            total_size += len(str(lyrics.translated_lyrics) or "")

        size_mb = total_size / (1024 * 1024)
        if size_mb < 1:
            return f"{total_size / 1024:.1f} KB"
        else:
            return f"{size_mb:.2f} MB"

    paginator = Paginator(songs_data, 20)
    page_number = request.GET.get("page")
    page_obj = paginator.get_page(page_number)

    context = {
        "page_obj": page_obj,
        "search_query": search_query,
        "total_songs": bookmarks.count(),
        "total_with_lyrics": sum(1 for s in songs_data if s["has_lyrics"]),
        "db_space_used": calculate_db_space(),
    }

    return render(request, "spotify/manage_songs.html", context)


@csrf_exempt
def spotify_delete_song(request):
    """Delete a bookmarked song and its lyrics"""
    if request.method != "POST":
        return JsonResponse({"success": False, "error": "Method not allowed"}, status=405)

    try:
        data = json.loads(request.body)
        bookmark_id = data.get("bookmark_id")

        if not bookmark_id:
            return JsonResponse({"success": False, "error": "Missing bookmark ID"})

        try:
            bookmark = BookmarkedSong.objects.get(id=bookmark_id)
        except BookmarkedSong.DoesNotExist:
            return JsonResponse({"success": False, "error": "Bookmark not found"})

        song_info = f"{bookmark.song_name} - {bookmark.artist_name}"

        lyrics = SongLyrics.get_lyrics(bookmark.song_name, bookmark.artist_name)
        if lyrics:
            lyrics.delete()

        bookmark.delete()

        return JsonResponse(
            {"success": True, "message": f"Successfully deleted {song_info} and its lyrics"}
        )

    except Exception as e:
        return JsonResponse({"success": False, "error": str(e)})


@csrf_exempt
def spotify_server_logs(request):
    """Fetch recent server logs for debugging"""
    if request.method != "GET":
        return JsonResponse({"success": False, "error": "Method not allowed"}, status=405)

    try:
        import subprocess
        import re

        try:
            yesterday = datetime.now() - timedelta(days=1)
            since_time = yesterday.strftime("%Y-%m-%d %H:%M:%S")

            result = subprocess.run(
                ["journalctl", "-u", "gunicorn", "--since", since_time, "-n", "50", "--no-pager"],
                capture_output=True,
                text=True,
                timeout=10,
            )

            if result.returncode == 0 and result.stdout:
                log_lines = result.stdout.strip().split("\n")
            else:
                try:
                    result = subprocess.run(
                        ["tail", "-n", "50", "/var/log/syslog"],
                        capture_output=True,
                        text=True,
                        timeout=10,
                    )

                    if result.returncode == 0 and result.stdout:
                        log_lines = [
                            line
                            for line in result.stdout.strip().split("\n")
                            if "gunicorn" in line or "error" in line.lower()
                        ]
                    else:
                        log_lines = []
                except Exception:
                    log_lines = []

        except Exception as e:
            print(f"Error getting logs: {e}")
            log_lines = []

        parsed_logs = []
        for line in log_lines[-50:]:
            if not line.strip():
                continue

            log_entry = {
                "timestamp": "",
                "level": "INFO",
                "message": line,
            }

            timestamp_patterns = [
                r"^(\w{3}\s+\d{1,2}\s+\d{2}:\d{2}:\d{2})",
                r"^(\d{4}-\d{2}-\d{2}\s+\d{2}:\d{2}:\d{2})",
            ]

            for pattern in timestamp_patterns:
                match = re.search(pattern, line)
                if match:
                    log_entry["timestamp"] = match.group(1)
                    break

            if "ERROR" in line.upper():
                log_entry["level"] = "ERROR"
            elif "WARNING" in line.upper() or "WARN" in line.upper():
                log_entry["level"] = "WARNING"
            elif "INFO" in line.upper():
                log_entry["level"] = "INFO"
            elif "DEBUG" in line.upper():
                log_entry["level"] = "DEBUG"

            parsed_logs.append(log_entry)

        if not parsed_logs:
            parsed_logs = [
                {
                    "timestamp": datetime.now().strftime("%Y-%m-%d %H:%M:%S"),
                    "level": "INFO",
                    "message": "No recent gunicorn logs found. Check if logging is properly configured.",
                },
                {
                    "timestamp": datetime.now().strftime("%Y-%m-%d %H:%M:%S"),
                    "level": "INFO",
                    "message": "Try running: journalctl -u gunicorn -f (to follow live logs)",
                },
            ]

        return JsonResponse(
            {
                "success": True,
                "logs": parsed_logs,
                "total_entries": len(parsed_logs),
            }
        )

    except Exception as e:
        return JsonResponse(
            {
                "success": False,
                "error": f"Failed to fetch server logs: {str(e)}",
                "logs": [],
            }
        )
