import os
import spotipy
from spotipy.oauth2 import SpotifyOAuth
from services import database

CACHE_PATH = os.path.join(os.path.dirname(os.path.dirname(os.path.abspath(__file__))), ".spotify_cache")

def get_spotify_credentials():
    """Get Spotify credentials from settings, falling back to environment variables."""
    client_id = database.get_setting("spotify_custom_client_id")
    client_secret = database.get_setting("spotify_custom_client_secret")
    redirect_uri = database.get_setting("spotify_custom_redirect_uri")

    # If database settings are empty, try environment variables (for initial run convenience)
    if not client_id:
        client_id = os.getenv("SPOTIFY_CLIENT_ID")
    if not client_secret:
        client_secret = os.getenv("SPOTIFY_CLIENT_SECRET")
    if not redirect_uri:
        redirect_uri = os.getenv("SPOTIFY_REDIRECT_URI", "http://localhost:8888/callback")
        
    return client_id, client_secret, redirect_uri

def get_oauth_manager():
    """Initialize and return SpotifyOAuth manager if credentials exist."""
    client_id, client_secret, redirect_uri = get_spotify_credentials()
    if not client_id or not client_secret:
        return None
        
    return SpotifyOAuth(
        client_id=client_id,
        client_secret=client_secret,
        redirect_uri=redirect_uri,
        scope="user-read-currently-playing user-read-playback-state",
        cache_path=CACHE_PATH,
        open_browser=True
    )

def is_authorized():
    """Check if the user is currently authorized with Spotify."""
    sp_oauth = get_oauth_manager()
    if not sp_oauth:
        return False
    token_info = sp_oauth.get_cached_token()
    if token_info and not sp_oauth.is_token_expired(token_info):
        return True
    # Try refreshing
    if token_info:
        try:
            sp_oauth.refresh_access_token(token_info['refresh_token'])
            return True
        except Exception:
            return False
    return False

def get_spotify_client():
    """Get an authorized Spotipy client, refreshing the token if needed."""
    sp_oauth = get_oauth_manager()
    if not sp_oauth:
        return None
        
    token_info = sp_oauth.validate_token(sp_oauth.get_cached_token())
    if not token_info:
        return None
        
    return spotipy.Spotify(auth=token_info['access_token'])

def get_current_song():
    """
    Fetch the currently playing song from Spotify.
    Returns:
        dict: song details, or None if nothing playing, or dict with error message.
    """
    sp = get_spotify_client()
    if not sp:
        return {"error": "Spotify is not authorized or credentials not configured. Please set them up in Settings."}
        
    try:
        database.increment_request_count("spotify")
        current_playback = sp.current_playback()
        
        if not current_playback or not current_playback.get("item"):
            return None  # Nothing playing
            
        track = current_playback["item"]
        song_name = track.get("name")
        artist_name = track["artists"][0]["name"] if track.get("artists") else "Unknown Artist"
        
        album_art_url = None
        if track.get("album") and track["album"].get("images"):
            album_art_url = track["album"]["images"][0]["url"]
            
        return {
            "song_name": song_name,
            "artist_name": artist_name,
            "album_art_url": album_art_url,
            "progress_ms": current_playback.get("progress_ms", 0),
            "duration_ms": track.get("duration_ms", 0),
            "is_playing": current_playback.get("is_playing", False)
        }
    except spotipy.SpotifyException as e:
        if e.http_status == 401:
            return {"error": "Spotify authorization expired. Please re-authenticate."}
        return {"error": f"Spotify API Error: {str(e)}"}
    except Exception as e:
        return {"error": f"Connection Error: {str(e)}"}
