# SpotLyric: Real-Time Spotify Lyrics, Translations & Romanization

SpotLyric is a personal companion app that automatically tracks what you are currently playing on Spotify, fetches the original lyrics, and uses Gemini AI to translate and romanize them on the fly. 

It is designed for multilingual music fans, making it easy to sing along to foreign language tracks (such as Hindi, Punjabi, Korean, Russian, Spanish, etc.) with line-by-line transliterations and translations.

SpotLyric is available as a **native Android app** and a **cross-platform Desktop GUI app**.

---

## Key Features

* **🎧 Real-Time Spotify Syncing**: Play a song in Spotify and open SpotLyric. The active track is detected automatically.
* **🧠 AI-Powered Translation & Romanization**: For non-English songs in non-Latin scripts, the app displays original lyrics, romanization, and translations side-by-side or stacked.
* **🔍 Multi-Stage Lyric Extraction**: Scrapes official lyrics platforms (Genius, LyricsRaag, LyricsWiz, LyricsDecoder, BollyMeaning) first, and falls back to Gemini AI for cleanup if needed.
* **⚡ Seamless Controls**: Adjust autoscroll speed (Slow, Fast, or manual) and swap lyric search sources on the fly.
* **💾 Data Backup & Portability**: Export and import your bookmarks, settings, and lyric history easily to transfer them between your phone and desktop.

---

## How to Install and Run

To use SpotLyric on either platform, you will need to input your API keys in the **Settings** screen (see the Setup guide below).

### 1. Android Application (Mobile)
A pre-compiled build is available in this repository:
1. Copy [app-debug.apk](file:///home/pseudo/work/app-debug.apk) to your Android device.
2. Install the APK (you may need to allow installations from unknown sources in your browser or file manager settings).
3. Alternatively, if your phone is plugged in with USB debugging enabled, you can install it from your computer by running:
   ```bash
   ./install_app.sh
   ```

### 2. Desktop GUI Application (Python)
Ensure Python 3.10+ and system-level **Tkinter** are installed:
* **Debian/Ubuntu**: `sudo apt-get install python3-tk`
* **Fedora**: `sudo dnf install python3-tkinter`
* **macOS**: `brew install python-tk`
* **Windows**: Bundled automatically with official Python installers.

Launch the app from the terminal using the launcher script:
```bash
./run_desktop.sh
```

---

## Setup & Configuration (First Time Run)

Both the Android and Desktop apps allow you to paste your custom credentials directly on their **Settings** page. All API keys are free.

### A. Spotify Integration
To allow SpotLyric to see what is playing:
1. Log in to the [Spotify Developer Dashboard](https://developer.spotify.com/dashboard).
2. Click **Create app**. Set **Redirect URI** to `http://localhost:8888/callback` (or your callback port).
3. Save the app and copy the **Client ID** and **Client Secret**.
4. Paste these into the settings page inside SpotLyric, save, and tap **Authorize**.

### B. Google Search (SerpAPI) Key
For finding lyrics links:
1. Get a free API key at [SerpAPI](https://serpapi.com).
2. Paste the key into settings (provides 100 free searches per month).

### C. Gemini AI Key
For translation and romanization:
1. Get a free API key at [Google AI Studio](https://aistudio.google.com).
2. Paste the key into settings.
