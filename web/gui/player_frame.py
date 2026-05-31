import os
import threading
import time
import requests
import io
from PIL import Image
import customtkinter as ctk
import tkinter as tk
from tkinter import messagebox

from services import database, spotify, search, pipeline

class PlayerFrame(ctk.CTkFrame):
    def __init__(self, parent, controller):
        super().__init__(parent)
        self.controller = controller
        
        # Grid layout: 2 rows (Now Playing Card, Lyrics View)
        self.grid_rowconfigure(1, weight=1)
        self.grid_columnconfigure(0, weight=1)
        
        # State variables
        self.current_track = None
        self.is_fetching = False
        self.lyrics_data = None
        self.autoscroll_job = None
        self.autoscroll_speed = 0  # 0: Off, 1: Slow, 2: Fast
        
        # --- Top: Now Playing Card ---
        self.setup_player_card()
        
        # --- Bottom: Lyrics View ---
        self.setup_lyrics_view()

        # Start Spotify polling thread
        self.stop_polling = False
        self.polling_thread = threading.Thread(target=self.poll_spotify, daemon=True)
        self.polling_thread.start()

    def setup_player_card(self):
        self.card_frame = ctk.CTkFrame(self, height=130, corner_radius=10, fg_color="#1e1e1e")
        self.card_frame.grid(row=0, column=0, padx=15, pady=10, sticky="ew")
        self.card_frame.grid_propagate(False)
        self.card_frame.grid_columnconfigure(1, weight=1)
        
        # Default placeholder album art
        self.default_art = ctk.CTkImage(
            light_image=Image.new("RGB", (100, 100), "#333333"),
            dark_image=Image.new("RGB", (100, 100), "#333333"),
            size=(100, 100)
        )
        
        self.album_art_label = ctk.CTkLabel(self.card_frame, image=self.default_art, text="")
        self.album_art_label.grid(row=0, column=0, rowspan=3, padx=15, pady=15)
        
        # Track Name & Artist
        self.track_name_label = ctk.CTkLabel(
            self.card_frame, text="Not Syncing", 
            font=ctk.CTkFont(size=18, weight="bold"), 
            anchor="w", justify="left"
        )
        self.track_name_label.grid(row=0, column=1, padx=5, pady=(15, 2), sticky="w")
        
        self.artist_label = ctk.CTkLabel(
            self.card_frame, text="Authorize Spotify in Settings to begin", 
            font=ctk.CTkFont(size=14, slant="italic"), 
            text_color="#888888", anchor="w", justify="left"
        )
        self.artist_label.grid(row=1, column=1, padx=5, pady=2, sticky="w")
        
        # Metadata / Source Badge
        self.badge_label = ctk.CTkLabel(
            self.card_frame, text="", 
            font=ctk.CTkFont(size=12), 
            text_color="#1DB954", anchor="w"
        )
        self.badge_label.grid(row=2, column=1, padx=5, pady=(2, 10), sticky="w")
        
        # Right Side: Action Controls
        self.controls_frame = ctk.CTkFrame(self.card_frame, fg_color="transparent")
        self.controls_frame.grid(row=0, column=2, rowspan=3, padx=15, pady=10, sticky="ns")
        
        self.swap_button = ctk.CTkButton(
            self.controls_frame, text="⇕ Sources", width=90, height=32, 
            fg_color="#333333", hover_color="#444444",
            command=self.open_source_dialog
        )
        self.swap_button.pack(pady=5)
        self.swap_button.configure(state="disabled")

        self.ai_translate_button = ctk.CTkButton(
            self.controls_frame, text="🧠 AI Trans", width=90, height=32,
            fg_color="#3a2a6b", hover_color="#4f388f",
            command=self.manual_ai_translate
        )
        self.ai_translate_button.pack(pady=5)
        self.ai_translate_button.configure(state="disabled")

    def setup_lyrics_view(self):
        # Frame holding scrollable content and controls
        self.lyrics_container = ctk.CTkFrame(self, corner_radius=10, fg_color="#121212")
        self.lyrics_container.grid(row=1, column=0, padx=15, pady=(0, 15), sticky="nsew")
        
        self.lyrics_container.grid_rowconfigure(1, weight=1)
        self.lyrics_container.grid_columnconfigure(0, weight=1)
        
        # Sub-header bar inside Lyrics container
        self.view_controls = ctk.CTkFrame(self.lyrics_container, fg_color="transparent", height=40)
        self.view_controls.grid(row=0, column=0, padx=10, pady=5, sticky="ew")
        
        # Autoscroll setting dropdown
        self.scroll_label = ctk.CTkLabel(self.view_controls, text="Autoscroll:", font=ctk.CTkFont(size=12))
        self.scroll_label.pack(side="left", padx=5)
        
        self.scroll_menu = ctk.CTkOptionMenu(
            self.view_controls, values=["Manual", "Slow Sync (1.0x)", "Fast Sync (2.0x)"],
            width=140, height=26, command=self.change_scroll_mode
        )
        self.scroll_menu.pack(side="left", padx=5)
        self.scroll_menu.set("Manual")

        # Scrollable lyrics area
        self.scroll_frame = ctk.CTkScrollableFrame(self.lyrics_container, fg_color="transparent")
        self.scroll_frame.grid(row=1, column=0, padx=10, pady=(0, 10), sticky="nsew")
        
        # Status loading overlay
        self.status_label = ctk.CTkLabel(
            self.scroll_frame, text="Ready. Play a track on Spotify.",
            font=ctk.CTkFont(size=16), text_color="#888888"
        )
        self.status_label.pack(expand=True, pady=100)

    # --- Spotify Polling & Syncing ---
    def poll_spotify(self):
        while not self.stop_polling:
            if not spotify.is_authorized():
                # Show instructions on GUI
                self.after(0, self.update_player_gui_offline)
                time.sleep(5)
                continue
                
            try:
                track = spotify.get_current_song()
                if track and "error" not in track:
                    # New track detected
                    track_id = f"{track['song_name']} - {track['artist_name']}"
                    if not self.current_track or self.current_track != track_id:
                        self.current_track = track_id
                        self.after(0, self.load_new_track_gui, track)
                        
                        # Load/fetch lyrics cache in thread
                        threading.Thread(target=self.load_lyrics_for_track, args=(track,), daemon=True).start()
                elif track and "error" in track:
                    self.after(0, self.update_player_gui_error, track["error"])
                else:
                    # Nothing playing
                    self.current_track = None
                    self.after(0, self.update_player_gui_idle)
            except Exception as e:
                print(f"Spotify polling exception: {e}")
                
            time.sleep(3)  # Poll every 3 seconds

    # --- GUI Updates (Main Thread) ---
    def update_player_gui_offline(self):
        self.track_name_label.configure(text="Spotify Offline")
        self.artist_label.configure(text="Please configure and authorize Spotify in Settings.")
        self.badge_label.configure(text="")
        self.album_art_label.configure(image=self.default_art)
        self.swap_button.configure(state="disabled")
        self.ai_translate_button.configure(state="disabled")

    def update_player_gui_error(self, err_msg):
        self.track_name_label.configure(text="Connection Issue")
        self.artist_label.configure(text=err_msg)
        self.badge_label.configure(text="")
        self.album_art_label.configure(image=self.default_art)

    def update_player_gui_idle(self):
        self.track_name_label.configure(text="No Track Playing")
        self.artist_label.configure(text="Play a song on Spotify to begin syncing.")
        self.badge_label.configure(text="")
        self.album_art_label.configure(image=self.default_art)
        self.swap_button.configure(state="disabled")
        self.ai_translate_button.configure(state="disabled")
        
        # Clear lyrics
        for child in self.scroll_frame.winfo_children():
            child.destroy()
        self.status_label = ctk.CTkLabel(
            self.scroll_frame, text="Ready. Play a track on Spotify.",
            font=ctk.CTkFont(size=16), text_color="#888888"
        )
        self.status_label.pack(expand=True, pady=100)

    def load_new_track_gui(self, track):
        self.track_name_label.configure(text=track["song_name"])
        self.artist_label.configure(text=track["artist_name"])
        self.badge_label.configure(text="Syncing lyrics...")
        
        # Load Art in Thread
        if track["album_art_url"]:
            threading.Thread(target=self.load_album_art, args=(track["album_art_url"],), daemon=True).start()
        else:
            self.album_art_label.configure(image=self.default_art)

    def load_album_art(self, url):
        try:
            response = requests.get(url, timeout=5)
            if response.status_code == 200:
                image_data = response.content
                image = Image.open(io.BytesIO(image_data))
                ctk_image = ctk.CTkImage(light_image=image, dark_image=image, size=(100, 100))
                self.after(0, lambda: self.album_art_label.configure(image=ctk_image))
        except Exception as e:
            print(f"Error loading album art: {e}")

    # --- Lyrics Loading & Fetching ---
    def load_lyrics_for_track(self, track):
        song = track["song_name"]
        artist = track["artist_name"]
        
        # Check SQLite Cache
        cached = database.get_lyrics(song, artist)
        if cached and cached.get("original_lyrics"):
            self.after(0, self.display_lyrics, cached)
            return

        # Not in cache, start scraping
        self.after(0, self.show_loading_lyrics, "Searching search engines for sources...")
        
        try:
            # 1. SerpAPI search
            sources = search.search_lyrics_sources(song, artist)
            if not sources:
                self.after(0, self.show_error_lyrics, "No lyrics sources found. Try manual switch sources.")
                return
                
            # Try ranked sources sequentially until one succeeds (Android Try Next Source behavior)
            self.after(0, self.show_loading_lyrics, f"Extracting lyrics from {search.normalise_url(sources[0]['url'])}...")
            
            success = False
            for idx, source in enumerate(sources):
                url = source["url"]
                self.after(0, self.show_loading_lyrics, f"Attempting source {idx+1}/{len(sources)}: {search.normalise_url(url)}")
                try:
                    lyrics = pipeline.extract_and_translate_lyrics(url, song, artist)
                    if lyrics and lyrics.get("original_lyrics"):
                        self.after(0, self.display_lyrics, lyrics)
                        success = True
                        break
                except Exception as e:
                    print(f"Failed extraction on source {url}: {e}")
                    continue
            
            if not success:
                self.after(0, self.show_error_lyrics, "Scraping pipeline failed for all sources.")
        except Exception as e:
            self.after(0, self.show_error_lyrics, str(e))

    def show_loading_lyrics(self, message):
        for child in self.scroll_frame.winfo_children():
            child.destroy()
        lbl = ctk.CTkLabel(self.scroll_frame, text=message, font=ctk.CTkFont(size=14), text_color="#1DB954")
        lbl.pack(pady=50)

    def show_error_lyrics(self, message):
        self.badge_label.configure(text="No Lyrics")
        self.swap_button.configure(state="normal")
        self.ai_translate_button.configure(state="disabled")
        for child in self.scroll_frame.winfo_children():
            child.destroy()
        lbl = ctk.CTkLabel(self.scroll_frame, text=message, font=ctk.CTkFont(size=14), text_color="#ff4a4a")
        lbl.pack(pady=50)

    def display_lyrics(self, lyrics):
        self.lyrics_data = lyrics
        
        # Enable Controls
        self.swap_button.configure(state="normal")
        
        # Check if we should enable manually translating (only if original lang is not english and AI translation is currently empty)
        is_english = (lyrics.get("original_language") or "").lower() == "en"
        has_trans = bool(lyrics.get("translated_lyrics")) or bool(lyrics.get("ai_translation"))
        if not is_english and not has_trans:
            self.ai_translate_button.configure(state="normal")
        else:
            self.ai_translate_button.configure(state="disabled")

        # Update Source badge
        domain = search.normalise_url(lyrics.get("source_url") or "Unknown Source")
        stage = lyrics.get("extraction_stage") or "Cached"
        conf = lyrics.get("confidence_score")
        conf_str = f" • Conf: {int(conf*100)}%" if conf is not None else ""
        self.badge_label.configure(text=f"Source: {domain}  •  Stage: {stage}{conf_str}")

        # Clear Scroll Frame
        for child in self.scroll_frame.winfo_children():
            child.destroy()

        original = lyrics["original_lyrics"]
        translated = lyrics.get("translated_lyrics") or lyrics.get("ai_translation") or ""
        romanized = lyrics.get("ai_romanized") or ""

        orig_lines = original.splitlines()
        trans_lines = translated.splitlines() if translated else []
        rom_lines = romanized.splitlines() if romanized else []

        # Create stacked lyrics view line-by-line
        max_lines = max(len(orig_lines), len(trans_lines), len(rom_lines))
        
        for i in range(max_lines):
            stanza_frame = ctk.CTkFrame(self.scroll_frame, fg_color="transparent")
            stanza_frame.pack(fill="x", pady=6)
            
            orig_text = orig_lines[i] if i < len(orig_lines) else ""
            rom_text = rom_lines[i] if i < len(rom_lines) else ""
            trans_text = trans_lines[i] if i < len(trans_lines) else ""
            
            # Skip if all lines are empty (stanza boundary)
            if not orig_text.strip() and not rom_text.strip() and not trans_text.strip():
                # Add extra spacing
                spacer = ctk.CTkLabel(stanza_frame, text="", height=10)
                spacer.pack()
                continue
            
            # Original Lyrics Line (White, Bold, 16px)
            if orig_text.strip():
                lbl_orig = ctk.CTkLabel(
                    stanza_frame, text=orig_text, 
                    font=ctk.CTkFont(size=16, weight="bold"), 
                    text_color="#ffffff", justify="center", anchor="center"
                )
                lbl_orig.pack(fill="x")
                
            # Romanized Line (Vibrant Blue, 13px)
            if rom_text.strip():
                lbl_rom = ctk.CTkLabel(
                    stanza_frame, text=rom_text, 
                    font=ctk.CTkFont(size=13), 
                    text_color="#4dabf7", justify="center", anchor="center"
                )
                lbl_rom.pack(fill="x")
                
            # Translated Line (Golden-Yellow, Slanted, 13px)
            if trans_text.strip():
                lbl_trans = ctk.CTkLabel(
                    stanza_frame, text=trans_text, 
                    font=ctk.CTkFont(size=13, slant="italic"), 
                    text_color="#ffd43b", justify="center", anchor="center"
                )
                lbl_trans.pack(fill="x")

        # Force scrollback to top
        self.after(50, lambda: self.scroll_frame._parent_canvas.yview_moveto(0))
        
        # Reset autoscroll position if running
        if self.autoscroll_speed > 0:
            self.restart_autoscroll()

    # --- Actions: Switch Source Dialog & AI Manual Translation ---
    def open_source_dialog(self):
        """Open the Source dialog popup window."""
        if not self.current_track:
            return
            
        song, artist = self.current_track.split(" - ", 1)
        from gui.source_dialog import SourceDialog
        dialog = SourceDialog(self.winfo_toplevel(), song, artist, self.on_source_swapped)
        dialog.grab_set()

    def on_source_swapped(self, selected_url):
        """Callback when user selects a source in the popup dialog."""
        if not self.current_track:
            return
        song, artist = self.current_track.split(" - ", 1)
        
        self.show_loading_lyrics(f"Re-extracting lyrics from: {search.normalise_url(selected_url)}")
        
        def run_reextract():
            try:
                lyrics = pipeline.extract_and_translate_lyrics(selected_url, song, artist)
                self.after(0, self.display_lyrics, lyrics)
            except Exception as e:
                self.after(0, self.show_error_lyrics, str(e))
                
        threading.Thread(target=run_reextract, daemon=True).start()

    def manual_ai_translate(self):
        """Trigger manual AI translation from Gemini."""
        if not self.lyrics_data:
            return
            
        self.ai_translate_button.configure(state="disabled")
        song = self.lyrics_data["song_name"]
        artist = self.lyrics_data["artist_name"]
        url = self.lyrics_data["source_url"]
        
        self.show_loading_lyrics("Generating Gemini AI Translation & Romanization...")
        
        def run_translate():
            try:
                lyrics = pipeline.generate_ai_translation_manual(song, artist, url)
                self.after(0, self.display_lyrics, lyrics)
            except Exception as e:
                self.after(0, lambda: messagebox.showerror("AI Translation Error", str(e)))
                self.after(0, self.display_lyrics, self.lyrics_data)
                
        threading.Thread(target=run_translate, daemon=True).start()

    # --- Autoscroll Controls ---
    def change_scroll_mode(self, mode):
        if "Slow" in mode:
            self.autoscroll_speed = 1
            self.restart_autoscroll()
        elif "Fast" in mode:
            self.autoscroll_speed = 2
            self.restart_autoscroll()
        else:
            self.autoscroll_speed = 0
            self.stop_autoscroll()

    def stop_autoscroll(self):
        if self.autoscroll_job:
            self.after_cancel(self.autoscroll_job)
            self.autoscroll_job = None

    def restart_autoscroll(self):
        self.stop_autoscroll()
        self.scroll_position = 0.0
        self.autoscroll_tick()

    def autoscroll_tick(self):
        if self.autoscroll_speed == 0 or not self.lyrics_data:
            return

        # Incremental movement: 1.0x (Slow) -> 0.0003 fractional increment, 2.0x (Fast) -> 0.0006
        increment = 0.00025 * self.autoscroll_speed
        self.scroll_position += increment
        
        if self.scroll_position > 1.0:
            self.scroll_position = 1.0
            
        self.scroll_frame._parent_canvas.yview_moveto(self.scroll_position)
        
        if self.scroll_position < 1.0:
            self.autoscroll_job = self.after(100, self.autoscroll_tick)

    def close(self):
        self.stop_polling = True
        self.stop_autoscroll()
