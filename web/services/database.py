import os
import sqlite3
import time
from datetime import datetime

DB_FILE = os.path.join(os.path.dirname(os.path.dirname(os.path.abspath(__file__))), "spotlyric.db")

def get_connection():
    """Get a database connection with foreign key support enabled."""
    conn = sqlite3.connect(DB_FILE)
    conn.row_factory = sqlite3.Row
    conn.execute("PRAGMA foreign_keys = ON;")
    return conn

def init_db():
    """Initialize database tables and default data."""
    with get_connection() as conn:
        # 1. bookmarked_song table
        conn.execute("""
            CREATE TABLE IF NOT EXISTS bookmarked_song (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                song_name TEXT NOT NULL,
                artist_name TEXT NOT NULL,
                bookmarked_url TEXT NOT NULL,
                title TEXT,
                UNIQUE(song_name, artist_name)
            );
        """)
        conn.execute("CREATE INDEX IF NOT EXISTS idx_bookmark_song_name ON bookmarked_song(song_name);")
        conn.execute("CREATE INDEX IF NOT EXISTS idx_bookmark_song_artist ON bookmarked_song(song_name, artist_name);")

        # 2. song_lyrics table
        conn.execute("""
            CREATE TABLE IF NOT EXISTS song_lyrics (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                bookmark_id INTEGER NOT NULL UNIQUE,
                original_lyrics TEXT NOT NULL,
                translated_lyrics TEXT,
                ai_romanized TEXT,
                ai_translation TEXT,
                source_url TEXT,
                extraction_stage TEXT,
                confidence_score REAL,
                original_language TEXT,
                FOREIGN KEY (bookmark_id) REFERENCES bookmarked_song(id) ON DELETE CASCADE
            );
        """)

        # 3. preferred_source table
        conn.execute("""
            CREATE TABLE IF NOT EXISTS preferred_source (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                domain TEXT NOT NULL UNIQUE,
                display_name TEXT NOT NULL,
                enabled INTEGER DEFAULT 1,
                added_at INTEGER NOT NULL
            );
        """)

        # 4. app_settings table
        conn.execute("""
            CREATE TABLE IF NOT EXISTS app_settings (
                key TEXT PRIMARY KEY,
                value TEXT
            );
        """)
        
        # Populate default settings if empty
        defaults = {
            "relevance_filter_enabled": "1",
            "relevance_threshold": "0.2",
            "gemini_custom_model": "gemini-2.5-flash-lite",
            "spotify_request_count": "0",
            "gemini_request_count": "0",
            "serpapi_request_count": "0",
            "last_tracked_month": ""
        }
        for k, v in defaults.items():
            conn.execute("INSERT OR IGNORE INTO app_settings (key, value) VALUES (?, ?);", (k, v))
            
        # Add default preferred domains
        default_domains = [
            ("genius.com", "Genius", 1),
            ("lyricsraag.com", "LyricsRaag", 1),
            ("musixmatch.com", "Musixmatch", 1),
            ("azlyrics.com", "AZLyrics", 1),
            ("lyricfinder.org", "LyricFinder", 1),
            ("letrastraducidas.org", "LetrasTraducidas", 1)
        ]
        now = int(time.time() * 1000)
        for domain, name, enabled in default_domains:
            conn.execute("""
                INSERT OR IGNORE INTO preferred_source (domain, display_name, enabled, added_at)
                VALUES (?, ?, ?, ?);
            """, (domain, name, enabled, now))
            
        conn.commit()

# --- Bookmarks ---
def add_bookmark(song_name, artist_name, bookmarked_url, title=None):
    """Add or replace a bookmark. Returns the row ID."""
    if not title:
        title = song_name
    with get_connection() as conn:
        cursor = conn.execute("""
            INSERT OR REPLACE INTO bookmarked_song (song_name, artist_name, bookmarked_url, title)
            VALUES (?, ?, ?, ?);
        """, (song_name, artist_name, bookmarked_url, title))
        conn.commit()
        return cursor.lastrowid

def delete_bookmark_by_id(bookmark_id):
    """Delete bookmark by ID (CASCADE deletes lyrics too)."""
    with get_connection() as conn:
        conn.execute("DELETE FROM bookmarked_song WHERE id = ?;", (bookmark_id,))
        conn.commit()

def delete_bookmark_by_name(song_name, artist_name):
    """Delete bookmark by song and artist name."""
    with get_connection() as conn:
        conn.execute("DELETE FROM bookmarked_song WHERE LOWER(song_name) = LOWER(?) AND LOWER(artist_name) = LOWER(?);", (song_name, artist_name))
        conn.commit()

def get_bookmark(song_name, artist_name):
    """Get bookmark details."""
    with get_connection() as conn:
        row = conn.execute("""
            SELECT * FROM bookmarked_song 
            WHERE LOWER(song_name) = LOWER(?) AND LOWER(artist_name) = LOWER(?);
        """, (song_name, artist_name)).fetchone()
        return dict(row) if row else None

def get_all_bookmarks(search_query=None):
    """Get all bookmarks, optionally filtered by search query."""
    with get_connection() as conn:
        if search_query:
            q = f"%{search_query}%"
            rows = conn.execute("""
                SELECT * FROM bookmarked_song 
                WHERE song_name LIKE ? OR artist_name LIKE ? OR title LIKE ?
                ORDER BY song_name ASC;
            """, (q, q, q)).fetchall()
        else:
            rows = conn.execute("SELECT * FROM bookmarked_song ORDER BY song_name ASC;").fetchall()
        return [dict(r) for r in rows]

def is_bookmarked(song_name, artist_name):
    """Check if a song is bookmarked."""
    return get_bookmark(song_name, artist_name) is not None

def get_bookmarked_urls(song_name, artist_name):
    """Get all bookmarked URLs for a song. Typically one URL, but returned as list for Android parity."""
    bookmark = get_bookmark(song_name, artist_name)
    return [bookmark["bookmarked_url"]] if bookmark else []

# --- Lyrics ---
def save_lyrics(bookmark_id, original_lyrics, translated_lyrics="", ai_romanized=None, ai_translation=None,
                source_url=None, extraction_stage=None, confidence_score=None, original_language=None):
    """Insert or update lyrics for a bookmark ID."""
    with get_connection() as conn:
        # Since bookmark_id is UNIQUE, delete old lyrics row to avoid clash
        conn.execute("DELETE FROM song_lyrics WHERE bookmark_id = ?;", (bookmark_id,))
        cursor = conn.execute("""
            INSERT INTO song_lyrics (
                bookmark_id, original_lyrics, translated_lyrics, ai_romanized, ai_translation,
                source_url, extraction_stage, confidence_score, original_language
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?);
        """, (bookmark_id, original_lyrics, translated_lyrics, ai_romanized, ai_translation,
              source_url, extraction_stage, confidence_score, original_language))
        conn.commit()
        return cursor.lastrowid

def get_lyrics(song_name, artist_name):
    """Get lyrics details for a song."""
    with get_connection() as conn:
        row = conn.execute("""
            SELECT l.*, b.song_name, b.artist_name FROM song_lyrics l
            JOIN bookmarked_song b ON l.bookmark_id = b.id
            WHERE LOWER(b.song_name) = LOWER(?) AND LOWER(b.artist_name) = LOWER(?);
        """, (song_name, artist_name)).fetchone()
        return dict(row) if row else None

def delete_lyrics_by_bookmark_id(bookmark_id):
    """Delete lyrics for a bookmark ID."""
    with get_connection() as conn:
        conn.execute("DELETE FROM song_lyrics WHERE bookmark_id = ?;", (bookmark_id,))
        conn.commit()

def update_ai_fields(bookmark_id, ai_romanized, ai_translation):
    """Update AI translation/romanization fields for a bookmark ID."""
    with get_connection() as conn:
        conn.execute("""
            UPDATE song_lyrics 
            SET ai_romanized = ?, ai_translation = ? 
            WHERE bookmark_id = ?;
        """, (ai_romanized, ai_translation, bookmark_id))
        conn.commit()

# --- Preferred Sources ---
def add_preferred_source(domain, display_name, enabled=1):
    """Add a preferred domain source."""
    now = int(time.time() * 1000)
    with get_connection() as conn:
        conn.execute("""
            INSERT OR REPLACE INTO preferred_source (domain, display_name, enabled, added_at)
            VALUES (?, ?, ?, ?);
        """, (domain.lower().strip(), display_name, 1 if enabled else 0, now))
        conn.commit()

def get_all_preferred_sources():
    """Get all preferred sources."""
    with get_connection() as conn:
        rows = conn.execute("SELECT * FROM preferred_source ORDER BY added_at ASC;").fetchall()
        return [dict(r) for r in rows]

def update_preferred_source(domain, enabled):
    """Update a source's enabled status."""
    with get_connection() as conn:
        conn.execute("UPDATE preferred_source SET enabled = ? WHERE domain = ?;", (1 if enabled else 0, domain.lower()))
        conn.commit()

def delete_preferred_source(domain):
    """Delete a preferred source."""
    with get_connection() as conn:
        conn.execute("DELETE FROM preferred_source WHERE domain = ?;", (domain.lower(),))
        conn.commit()

# --- Settings ---
def get_setting(key, default=None):
    """Get a configuration setting."""
    with get_connection() as conn:
        row = conn.execute("SELECT value FROM app_settings WHERE key = ?;", (key,)).fetchone()
        return row["value"] if row else default

def set_setting(key, value):
    """Set a configuration setting."""
    with get_connection() as conn:
        conn.execute("INSERT OR REPLACE INTO app_settings (key, value) VALUES (?, ?);", (key, str(value) if value is not None else None))
        conn.commit()

# --- Request Counters (Monthly tracker) ---
def _check_month_reset(conn):
    """Reset counters if the calendar month has changed."""
    current_month = datetime.now().strftime("%Y-%m")
    row = conn.execute("SELECT value FROM app_settings WHERE key = 'last_tracked_month';").fetchone()
    stored_month = row["value"] if row else ""
    if stored_month != current_month:
        conn.execute("UPDATE app_settings SET value = '0' WHERE key IN ('spotify_request_count', 'gemini_request_count', 'serpapi_request_count');")
        conn.execute("INSERT OR REPLACE INTO app_settings (key, value) VALUES ('last_tracked_month', ?);", (current_month,))
        return True
    return False

def get_request_counts():
    """Get monthly request counts for Spotify, Gemini, and SerpAPI."""
    with get_connection() as conn:
        _check_month_reset(conn)
        spotify = int(conn.execute("SELECT value FROM app_settings WHERE key = 'spotify_request_count';").fetchone()["value"] or 0)
        gemini = int(conn.execute("SELECT value FROM app_settings WHERE key = 'gemini_request_count';").fetchone()["value"] or 0)
        serpapi = int(conn.execute("SELECT value FROM app_settings WHERE key = 'serpapi_request_count';").fetchone()["value"] or 0)
        return {"spotify": spotify, "gemini": gemini, "serpapi": serpapi}

def increment_request_count(service_name):
    """Increment request count for 'spotify', 'gemini', or 'serpapi'."""
    key = f"{service_name}_request_count"
    with get_connection() as conn:
        _check_month_reset(conn)
        row = conn.execute("SELECT value FROM app_settings WHERE key = ?;", (key,)).fetchone()
        val = int(row["value"] if row else 0) + 1
        conn.execute("INSERT OR REPLACE INTO app_settings (key, value) VALUES (?, ?);", (key, str(val)))
        conn.commit()
