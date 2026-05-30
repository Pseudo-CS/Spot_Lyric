from typing import Dict, Tuple, Optional
import time
import requests
import os
from dotenv import load_dotenv
from serpapi import GoogleSearch
from .models import BookmarkedSong

# Load environment variables
load_dotenv()

# SerpAPI credentials
SERPAPI_KEY = os.getenv("SERPAPI_KEY")


def get_cached_bookmarks(song_name: str, artist_name: str) -> Dict[str, Dict]:
    """Get cached bookmarks for a song and artist"""
    try:
        bookmarked_url = BookmarkedSong.get_bookmarked_url(song_name, artist_name)
        if bookmarked_url:
            bookmark = BookmarkedSong.objects.filter(
                song_name__iexact=song_name,
                artist_name__iexact=artist_name,
            ).first()

            return {
                bookmarked_url: {
                    "title": bookmark.title
                }
                if bookmark and bookmark.title
                else True
            }
        return {}
    except Exception as e:
        print(f"Error getting cached bookmarks: {e}")
        return {}


def save_bookmark(song_name: str, artist_name: str, url: str, title: str = None) -> bool:
    """Save a bookmark for a song and artist"""
    try:
        BookmarkedSong.save_bookmark(song_name, artist_name, url, title)
        return True
    except Exception as e:
        print(f"Error saving bookmark: {e}")
        return False


def remove_bookmark(song_name: str, artist_name: str, url: str) -> bool:
    """Remove a bookmark for a song and artist"""
    try:
        deleted_count, _ = BookmarkedSong.objects.filter(
            song_name__iexact=song_name,
            artist_name__iexact=artist_name,
            bookmarked_url=url,
        ).delete()
        return deleted_count > 0
    except Exception as e:
        print(f"Error removing bookmark: {e}")
        return False


def toggle_bookmark(song_name: str, artist_name: str, url: str, title: str = None) -> Tuple[bool, bool]:
    """Toggle bookmark status for a URL. Returns (success, is_bookmarked)"""
    try:
        bookmark = BookmarkedSong.objects.filter(
            song_name__iexact=song_name,
            artist_name__iexact=artist_name,
        ).first()

        if bookmark:
            bookmark.delete()
            return True, False
        else:
            BookmarkedSong.save_bookmark(song_name, artist_name, url, title)
            return True, True
    except Exception as e:
        print(f"Error toggling bookmark: {e}")
        return False, False


def cleanup_expired_cache():
    """Legacy function - no longer needed with minimal model but keeping for compatibility"""
    return 0


def search_original_lyrics(song_name: str, artist_name: str) -> Optional[Dict]:
    """
    Search for original lyrics using SERP API
    Returns the first promising lyrics URL and content
    """
    if not SERPAPI_KEY:
        return {"error": "SERP API key not configured"}

    search_query = f"{song_name} {artist_name} lyrics original"

    params = {
        "engine": "google",
        "q": search_query,
        "api_key": SERPAPI_KEY,
        "num": 5,
        "gl": "in",
        "hl": "en",
    }

    try:
        time.sleep(1)
        search = GoogleSearch(params)
        results = search.get_dict()

        if "error" in results or "organic_results" not in results:
            return {"error": "No search results found"}

        for result in results["organic_results"]:
            url = result.get("link", "")
            title = result.get("title", "")

            if any(
                site in url.lower()
                for site in [
                    "genius.com",
                    "azlyrics.com",
                    "metrolyrics.com",
                    "lyrics.com",
                    "lyricfind.com",
                ]
            ):
                lyrics_content = scrape_lyrics_content(url)
                if lyrics_content:
                    return {
                        "url": url,
                        "title": title,
                        "content": lyrics_content,
                        "source": "serp_api",
                    }

        first_result = results["organic_results"][0]
        return {
            "url": first_result.get("link", ""),
            "title": first_result.get("title", ""),
            "content": None,
            "source": "serp_api",
        }

    except Exception as e:
        return {"error": f"SERP API search failed: {str(e)}"}


def scrape_lyrics_content(url: str) -> Optional[str]:
    """
    Simple lyrics content scraper
    Returns raw text content for further processing
    """
    try:
        headers = {
            "User-Agent": (
                "Mozilla/5.0 (Windows NT 10.0; Win64; x64) "
                "AppleWebKit/537.36 (KHTML, like Gecko) "
                "Chrome/91.0.4472.124 Safari/537.36"
            )
        }

        response = requests.get(url, headers=headers, timeout=10)
        response.raise_for_status()

        return response.text

    except Exception as e:
        print(f"Error scraping {url}: {e}")
        return None
