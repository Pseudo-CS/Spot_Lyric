# SpotLyric: Real-Time Spotify Lyrics, Translations & Romanization

SpotLyric is a personal companion app that enhances your music listening experience on Spotify. It automatically tracks your currently playing song, fetches the original lyrics, and uses Gemini AI to translate them and provide easy-to-read phonetic romanization (transliteration) for foreign language tracks (such as Hindi, Punjabi, Korean, Russian, etc.) so you can sing along instantly.

SpotLyric is available as a **native Android app** and a **responsive Web dashboard**.

---

## Key Features

### 🎧 Real-Time Spotify Syncing
Simply play a song in Spotify and open SpotLyric. The app immediately detects the active track and displays your lyrics without any manual typing or search queries.

### 🧠 AI-Powered Translation & Romanization
* **Sing Along in Any Language**: For non-English songs in non-Latin scripts, the app automatically generates line-by-line translations and romanizations side-by-side.
* **Smart Language Detection**: Skips translations when the song is already in English or when translated lyrics are already available.
* **Trimming & Safeguards**: AI processing is split for longer tracks so lyrics are never cut off.

### 🔍 Multi-Stage Lyric Extraction
The app searches the web using high-accuracy queries and extracts lyrics from pages through a 3-stage intelligence system:
1. **Official Site Parsers**: Tailored extraction for major platforms like Genius and LyricsRaag.
2. **Structural Heuristics**: Smart code patterns that isolate song text from other page clutter.
3. **Generative AI Parsing**: A fallback mechanism that uses Gemini AI to clean up raw web text.

### ⚡ Seamless Control & Tuning
* **Adjustable Autoscroll**: Choose between Slow, Fast, or manual scrolling to match the song's tempo.
* **Instant Source Swapping**: If you don't like the look of a lyric source, tap the swap button (⇕) to select other search results and re-extract on the fly.
* **Screen Wake Lock**: Keep your phone's screen from turning off while reading lyrics.

### 💾 Safe Data Management & Backups
* **Bookmarks & Offline Cache**: Save your favorite song lyrics to access them offline.
* **Privacy-First Backups**: Export and import your settings and translation history using Android's native file picker without giving the app permission to read your device's files.
* **Data Safeguard Shield**: If the app updates its database, it automatically migrates and restores your saved tracks so you never lose your library.

---

## Setup & Run

To run SpotLyric, you need API keys from **Spotify (Developer Account)**, **SerpAPI (Google Search)**, and **Google AI Studio (Gemini API)**.

### Android Application
1. Download or import the project directory `/android` in Android Studio.
2. In your root `local.properties` (or `secrets.properties`), insert your credentials:
   ```properties
   SPOTIFY_CLIENT_ID=your_spotify_client_id
   SERPAPI_KEY=your_serpapi_key
   GEMINI_API_KEY=your_gemini_api_key
   ```
3. Run the app on your Android device or emulator. Go to settings inside the app to customize search preferences.

### Web Dashboard (Django)
1. Navigate to the `web` directory:
   ```bash
   cd web
   ```
2. Set up your virtual environment and install dependencies:
   ```bash
   python -m venv .venv
   source .venv/bin/activate  # Or .venv\Scripts\activate on Windows
   pip install -r requirements.txt
   ```
3. Copy `.env.example` to `.env` and fill in your keys:
   ```env
   SPOTIFY_CLIENT_ID=your_spotify_client_id
   SPOTIFY_CLIENT_SECRET=your_spotify_client_secret
   SPOTIFY_REDIRECT_URI=http://127.0.0.1:8000/spotify/callback
   SERPAPI_KEY=your_serpapi_key
   GEMINI_API_KEY=your_gemini_api_key
   ```
4. Run migrations and start the Django server:
   ```bash
   python manage.py migrate
   python manage.py runserver
   ```
5. Open your browser and navigate to `http://127.0.0.1:8000/spotify/` to log in and start syncing.
