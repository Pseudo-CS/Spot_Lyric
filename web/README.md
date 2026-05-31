# SpotLyric Desktop Application

SpotLyric is a personal companion app that automatically tracks what you are currently playing on Spotify and displays original lyrics alongside real-time line-by-line translations and phonetic romanizations (transliterations). 

It is designed to help you sing along to foreign language tracks (such as Hindi, Punjabi, Russian, Korean, Spanish, etc.) by automatically translating and romanizing them on the fly.

---

## Key Features

* **🎧 Real-Time Spotify Syncing**: Play a song on Spotify (desktop or mobile) and open SpotLyric. The app automatically detects your active track and retrieves its lyrics.
* **🧠 AI Translation & Romanization**: Non-English tracks are automatically translated line-by-line and converted into Latin script so you can read and pronounce the lyrics.
* **🔍 Instant Source Swapping**: If a source has typos or incorrect lines, click the **⇕ Sources** button to select another Google search result and re-extract the lyrics.
* **⚡ Adjustable Autoscroll**: Switch between Slow, Fast, or manual scrolling to match the song's tempo.
* **💾 Bookmark Library & Cache**: Save your favorite songs to read them offline.
* **📥 Backup & Import**: Easily transfer your bookmarked lyrics and settings between devices using local backup files.

---

## How to Install and Run

### Step 1: Install Python & Tkinter (Prerequisites)
Make sure you have **Python 3.10+** installed. You will also need the system **Tkinter** package.

* **Debian/Ubuntu Linux**:
  ```bash
  sudo apt-get update
  sudo apt-get install python3-tk
  ```
* **Fedora/CentOS/RHEL**:
  ```bash
  sudo dnf install python3-tkinter
  ```
* **Arch Linux**:
  ```bash
  sudo pacman -S tk
  ```
* **macOS** (using Homebrew):
  ```bash
  brew install python-tk
  ```
* **Windows**:
  Tkinter is bundled automatically when you install Python from the official website (make sure to check "tcl/tk and IDLE" during setup).

### Step 2: Launch the App
Open your terminal in the project folder and run the launcher script:
```bash
./run_desktop.sh
```
*(Windows users can open command prompt in the `web` folder and run `python main.py` after running `pip install -r requirements.txt`.)*

---

## Setup Instructions (First Time Run)

When you first launch the app, go to the **Settings** tab to authorize Spotify and input your API keys. All keys can be obtained for free.

### 1. Spotify Sync Setup
To enable real-time tracking, you need a Spotify developer credential:
1. Go to the [Spotify Developer Dashboard](https://developer.spotify.com/dashboard).
2. Log in and click **Create app**.
3. Fill in a name and description.
4. Set **Redirect URI** to `http://localhost:8888/callback` (or your preferred port callback).
5. Check the **Web API** box, accept the terms, and save.
6. Copy your **Client ID** and **Client Secret**.
7. Paste these into the **Settings** tab in SpotLyric, click **Save Settings**, and click **Authorize Spotify**. A browser window will open to link your account.

### 2. Google Search (SerpAPI) Setup
To search the web for lyrics, SpotLyric uses SerpAPI:
1. Register for a free account at [SerpAPI](https://serpapi.com).
2. Copy your API key from your account dashboard.
3. Paste the key into the **SerpAPI Key** field in settings and click **Save Settings**.
*(Note: The free tier provides 100 searches per month, which is tracked on the Settings dashboard).*

### 3. Gemini AI Setup
For translation and romanization features, SpotLyric uses the Gemini API:
1. Obtain a free API key at [Google AI Studio](https://aistudio.google.com).
2. Paste the key into the **Gemini API Key** field in settings and click **Save Settings**.
