import os
import sys
import customtkinter as ctk

# Ensure correct imports
sys.path.append(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))

from services import database
from gui.player_frame import PlayerFrame
from gui.bookmarks_frame import BookmarksFrame
from gui.sources_frame import SourcesFrame
from gui.settings_frame import SettingsFrame

class SpotLyricApp(ctk.CTk):
    def __init__(self):
        super().__init__()
        
        # Configure window
        self.title("SpotLyric: Real-Time Spotify Lyrics & Translations")
        self.geometry("1000x650")
        self.minsize(900, 580)
        
        # Set dark theme
        ctk.set_appearance_mode("dark")
        ctk.set_default_color_theme("blue")
        
        # Initialize SQLite database and tables
        database.init_db()
        
        # Configure layout: 1 row, sidebar column and content column
        self.grid_rowconfigure(0, weight=1)
        self.grid_columnconfigure(0, weight=0)  # Sidebar
        self.grid_columnconfigure(1, weight=1)  # Main Content Area
        
        # Create Sidebar
        self.setup_sidebar()
        
        # Create Content Container Frame
        self.container = ctk.CTkFrame(self, fg_color="transparent")
        self.container.grid(row=0, column=1, sticky="nsew")
        self.container.grid_rowconfigure(0, weight=1)
        self.container.grid_columnconfigure(0, weight=1)
        
        # Initialize sub-frames
        self.frames = {}
        for F in (PlayerFrame, BookmarksFrame, SourcesFrame, SettingsFrame):
            frame_name = F.__name__
            frame = F(parent=self.container, controller=self)
            self.frames[frame_name] = frame
            frame.grid(row=0, column=0, sticky="nsew")
            
        # Switch to Now Playing by default
        self.show_frame("PlayerFrame")

    def setup_sidebar(self):
        self.sidebar = ctk.CTkFrame(self, width=200, corner_radius=0, fg_color="#090909")
        self.sidebar.grid(row=0, column=0, sticky="nsew")
        self.sidebar.grid_rowconfigure(5, weight=1)  # Push bottom labels down
        
        # Title Label
        self.title_lbl = ctk.CTkLabel(
            self.sidebar, text="SpotLyric", 
            font=ctk.CTkFont(size=22, weight="bold"), 
            text_color="#1DB954"
        )
        self.title_lbl.grid(row=0, column=0, padx=20, pady=25)
        
        # Navigation Buttons
        self.nav_buttons = {}
        
        navs = [
            ("Now Playing", "PlayerFrame"),
            ("Bookmarks", "BookmarksFrame"),
            ("Preferred Sources", "SourcesFrame"),
            ("Settings", "SettingsFrame")
        ]
        
        for idx, (label, target_frame) in enumerate(navs):
            btn = ctk.CTkButton(
                self.sidebar, text=label, height=40, corner_radius=6,
                fg_color="transparent", text_color="#cccccc", hover_color="#2b2b2b",
                font=ctk.CTkFont(size=14), anchor="w",
                command=lambda f=target_frame: self.show_frame(f)
            )
            btn.grid(row=idx+1, column=0, padx=15, pady=6, sticky="ew")
            self.nav_buttons[target_frame] = btn

        # Version Info Label at bottom
        self.ver_lbl = ctk.CTkLabel(self.sidebar, text="v1.0.0 (Python CLI/GUI)", font=ctk.CTkFont(size=11), text_color="#444444")
        self.ver_lbl.grid(row=6, column=0, pady=15)

    def show_frame(self, frame_name):
        """Bring frame to top and update sidebar button styling."""
        frame = self.frames[frame_name]
        frame.tkraise()
        
        # Update sidebar button states
        for key, btn in self.nav_buttons.items():
            if key == frame_name:
                btn.configure(fg_color="#1DB954", text_color="#ffffff", hover_color="#179b46")
            else:
                btn.configure(fg_color="transparent", text_color="#cccccc", hover_color="#2b2b2b")
                
        # Trigger frame-specific refresh functions on switch
        if frame_name == "BookmarksFrame":
            frame.refresh_all()
        elif frame_name == "SourcesFrame":
            frame.refresh_list()
        elif frame_name == "SettingsFrame":
            frame.refresh_stats()

    # --- Navigation Helpers called from other sub-frames ---
    def load_cached_lyrics(self, song_name, artist_name, url):
        """Switch to Player frame and load selected song lyrics."""
        player = self.frames["PlayerFrame"]
        
        # Update player track values
        player.current_track = f"{song_name} - {artist_name}"
        player.track_name_label.configure(text=song_name)
        player.artist_label.configure(text=artist_name)
        player.badge_label.configure(text="Loading bookmarked lyrics...")
        
        # Switch tab
        self.show_frame("PlayerFrame")
        
        # Load and display in background
        def run_load():
            # Check database for lyrics
            lyrics = database.get_lyrics(song_name, artist_name)
            if lyrics and lyrics.get("original_lyrics"):
                self.after(0, player.display_lyrics, lyrics)
            else:
                # If lyrics are not cached, fetch them using the url bookmark
                player.load_lyrics_for_track({
                    "song_name": song_name,
                    "artist_name": artist_name,
                    "album_art_url": None
                })
                
        import threading
        threading.Thread(target=run_load, daemon=True).start()

    def refresh_other_tabs(self):
        """Refresh other tabs if data changed (e.g. backup imported)."""
        self.frames["BookmarksFrame"].refresh_all()
        self.frames["SourcesFrame"].refresh_list()
        self.frames["SettingsFrame"].load_settings()

    def on_closing(self):
        # Shut down background polling threads
        self.frames["PlayerFrame"].close()
        self.destroy()

if __name__ == "__main__":
    app = SpotLyricApp()
    app.protocol("WM_DELETE_WINDOW", app.on_closing)
    app.mainloop()
