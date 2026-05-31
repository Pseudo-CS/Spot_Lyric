import json
import os
import time
from services import database

def export_data(file_path):
    """
    Export all user bookmarks, lyrics cache, preferred sources, and settings into a JSON backup file.
    Matches the schema format of BackupRepositoryImpl in Android.
    """
    try:
        # 1. Fetch DB records
        bookmarks = database.get_all_bookmarks()
        preferred_sources = database.get_all_preferred_sources()
        
        # Assemble bookmarks with lyrics nested
        backup_bookmarks = []
        for bookmark in bookmarks:
            lyrics = database.get_lyrics(bookmark["song_name"], bookmark["artist_name"])
            
            backup_lyrics = None
            if lyrics:
                backup_lyrics = {
                    "originalLyrics": lyrics.get("original_lyrics") or "",
                    "translatedLyrics": lyrics.get("translated_lyrics") or "",
                    "aiRomanized": lyrics.get("ai_romanized"),
                    "aiTranslation": lyrics.get("ai_translation"),
                    "sourceUrl": lyrics.get("source_url"),
                    "extractionStage": lyrics.get("extraction_stage"),
                    "confidenceScore": lyrics.get("confidence_score"),
                    "originalLanguage": lyrics.get("original_language")
                }
                
            backup_bookmarks.append({
                "songName": bookmark["song_name"],
                "artistName": bookmark["artist_name"],
                "bookmarkedUrl": bookmark["bookmarked_url"],
                "title": bookmark["title"] or "",
                "lyrics": backup_lyrics
            })

        # Assemble preferred sources
        backup_sources = []
        for source in preferred_sources:
            backup_sources.append({
                "domain": source["domain"],
                "displayName": source["display_name"],
                "enabled": bool(source["enabled"])
            })

        # Fetch current settings
        backup_settings = {
            "relevanceFilterEnabled": int(database.get_setting("relevance_filter_enabled", 1)) == 1,
            "relevanceThreshold": float(database.get_setting("relevance_threshold", 0.2)),
            "spotifyCustomClientId": database.get_setting("spotify_custom_client_id"),
            "spotifyCustomClientSecret": database.get_setting("spotify_custom_client_secret"),
            "spotifyCustomRedirectUri": database.get_setting("spotify_custom_redirect_uri"),
            "serpApiCustomApiKey": database.get_setting("serpapi_custom_api_key"),
            "geminiCustomApiKey": database.get_setting("gemini_custom_api_key"),
            "geminiCustomModel": database.get_setting("gemini_custom_model", "gemini-2.5-flash-lite")
        }

        # Final backup structure
        backup_data = {
            "version": 1,
            "bookmarks": backup_bookmarks,
            "preferredSources": backup_sources,
            "settings": backup_settings
        }

        with open(file_path, "w", encoding="utf-8") as f:
            json.dump(backup_data, f, indent=4, ensure_ascii=False)

        return {"success": True, "message": f"Backup successfully exported to {os.path.basename(file_path)}"}
    except Exception as e:
        return {"success": False, "error": str(e)}

def import_data(file_path):
    """
    Import user bookmarks, lyrics cache, preferred sources, and settings from a JSON backup file.
    Matches parsing of BackupRepositoryImpl in Android (handles both new structure and legacy arrays).
    """
    try:
        if not os.path.exists(file_path):
            return {"success": False, "error": "File does not exist."}
            
        with open(file_path, "r", encoding="utf-8") as f:
            content = f.read().strip()
            
        if not content:
            return {"success": False, "error": "Backup file is empty."}

        # Parse JSON
        data = json.loads(content)
        
        # 1. Handle Legacy JSON array format of bookmarks
        if isinstance(data, list):
            backup_bookmarks = []
            for item in data:
                song = item.get("song")
                if not song:
                    continue
                lyrics = item.get("lyrics")
                
                backup_lyrics = None
                if lyrics:
                    backup_lyrics = {
                        "originalLyrics": lyrics.get("originalLyrics") or "",
                        "translatedLyrics": lyrics.get("translatedLyrics") or "",
                        "aiRomanized": lyrics.get("aiRomanized"),
                        "aiTranslation": lyrics.get("aiTranslation")
                    }
                    
                backup_bookmarks.append({
                    "songName": song.get("songName") or "",
                    "artistName": song.get("artistName") or "",
                    "bookmarkedUrl": song.get("bookmarkedUrl") or "",
                    "title": song.get("title") or "",
                    "lyrics": backup_lyrics
                })
            backup_data = {
                "version": 1,
                "bookmarks": backup_bookmarks,
                "preferredSources": [],
                "settings": {}
            }
        else:
            backup_data = data

        # 2. Import Preferred Sources
        for source in backup_data.get("preferredSources", []):
            domain = source.get("domain")
            display_name = source.get("displayName")
            enabled = 1 if source.get("enabled", True) else 0
            if domain and display_name:
                database.add_preferred_source(domain, display_name, enabled)

        # 3. Import Settings (if dict format)
        settings = backup_data.get("settings", {})
        if settings:
            if "relevanceFilterEnabled" in settings:
                database.set_setting("relevance_filter_enabled", 1 if settings["relevanceFilterEnabled"] else 0)
            if "relevanceThreshold" in settings:
                database.set_setting("relevance_threshold", settings["relevanceThreshold"])
            if "spotifyCustomClientId" in settings:
                database.set_setting("spotify_custom_client_id", settings["spotifyCustomClientId"])
            if "spotifyCustomClientSecret" in settings:
                database.set_setting("spotify_custom_client_secret", settings["spotifyCustomClientSecret"])
            if "spotifyCustomRedirectUri" in settings:
                database.set_setting("spotify_custom_redirect_uri", settings["spotifyCustomRedirectUri"])
            if "serpApiCustomApiKey" in settings:
                database.set_setting("serpapi_custom_api_key", settings["serpApiCustomApiKey"])
            if "geminiCustomApiKey" in settings:
                database.set_setting("gemini_custom_api_key", settings["geminiCustomApiKey"])
            if "geminiCustomModel" in settings:
                database.set_setting("gemini_custom_model", settings["geminiCustomModel"])

        # 4. Import Bookmarks & Lyrics (overwriting if existing)
        for backup_bookmark in backup_data.get("bookmarks", []):
            song_name = backup_bookmark.get("songName")
            artist_name = backup_bookmark.get("artistName")
            url = backup_bookmark.get("bookmarkedUrl") or ""
            title = backup_bookmark.get("title") or song_name
            
            if not song_name:
                continue

            # Delete existing if match (same behavior as Android to avoid clashes)
            existing = database.get_bookmark(song_name, artist_name)
            if existing:
                database.delete_bookmark_by_id(existing["id"])

            # Insert bookmark
            new_bookmark_id = database.add_bookmark(song_name, artist_name, url, title)

            # Insert lyrics
            lyrics_data = backup_bookmark.get("lyrics")
            if lyrics_data:
                database.save_lyrics(
                    bookmark_id=new_bookmark_id,
                    original_lyrics=lyrics_data.get("originalLyrics") or "",
                    translated_lyrics=lyrics_data.get("translatedLyrics") or "",
                    ai_romanized=lyrics_data.get("aiRomanized"),
                    ai_translation=lyrics_data.get("aiTranslation"),
                    source_url=lyrics_data.get("sourceUrl"),
                    extraction_stage=lyrics_data.get("extractionStage"),
                    confidence_score=lyrics_data.get("confidenceScore"),
                    original_language=lyrics_data.get("originalLanguage")
                )

        return {"success": True, "message": "Backup imported successfully. Bookmarks and Settings restored."}
    except Exception as e:
        return {"success": False, "error": str(e)}
