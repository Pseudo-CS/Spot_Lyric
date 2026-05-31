import os
import customtkinter as ctk
from tkinter import filedialog, messagebox

from services import database, backup

class BookmarksFrame(ctk.CTkFrame):
    def __init__(self, parent, controller):
        super().__init__(parent)
        self.controller = controller
        
        # Grid layout: left side (bookmark list), right side (stats & backup cards)
        self.grid_columnconfigure(0, weight=3)
        self.grid_columnconfigure(1, weight=1)
        self.grid_rowconfigure(0, weight=1)
        
        # --- Left Side: Bookmark List ---
        self.left_container = ctk.CTkFrame(self, fg_color="transparent")
        self.left_container.grid(row=0, column=0, padx=15, pady=15, sticky="nsew")
        self.left_container.grid_rowconfigure(1, weight=1)
        self.left_container.grid_columnconfigure(0, weight=1)
        
        # Search bar
        self.search_frame = ctk.CTkFrame(self.left_container, fg_color="transparent")
        self.search_frame.grid(row=0, column=0, pady=(0, 10), sticky="ew")
        
        self.search_entry = ctk.CTkEntry(
            self.search_frame, placeholder_text="Search bookmarks by song, artist...", 
            height=36
        )
        self.search_entry.pack(side="left", fill="x", expand=True, padx=(0, 10))
        self.search_entry.bind("<KeyRelease>", self.on_search_key)
        
        self.search_btn = ctk.CTkButton(
            self.search_frame, text="Search", width=80, height=36, 
            fg_color="#333333", hover_color="#444444", command=self.refresh_list
        )
        self.search_btn.pack(side="right")
        
        # Scrollable bookmark list
        self.list_scroll = ctk.CTkScrollableFrame(self.left_container, fg_color="#121212")
        self.list_scroll.grid(row=1, column=0, sticky="nsew")
        
        # --- Right Side: Stats & Backups ---
        self.right_container = ctk.CTkFrame(self, fg_color="transparent")
        self.right_container.grid(row=0, column=1, padx=15, pady=15, sticky="nsew")
        
        # Card 1: DB Space & Statistics
        self.stats_card = ctk.CTkFrame(self.right_container, fg_color="#1e1e1e", corner_radius=10)
        self.stats_card.pack(fill="x", pady=(0, 15))
        
        self.stats_title = ctk.CTkLabel(
            self.stats_card, text="LIBRARY METRICS", 
            font=ctk.CTkFont(size=14, weight="bold"), text_color="#1DB954"
        )
        self.stats_title.pack(anchor="w", padx=15, pady=(15, 10))
        
        self.total_bookmarks_lbl = ctk.CTkLabel(self.stats_card, text="Bookmarked Songs: --", font=ctk.CTkFont(size=13))
        self.total_bookmarks_lbl.pack(anchor="w", padx=15, pady=3)
        
        self.with_lyrics_lbl = ctk.CTkLabel(self.stats_card, text="Cached Lyrics: --", font=ctk.CTkFont(size=13))
        self.with_lyrics_lbl.pack(anchor="w", padx=15, pady=3)
        
        self.db_space_lbl = ctk.CTkLabel(self.stats_card, text="Database Space: --", font=ctk.CTkFont(size=13))
        self.db_space_lbl.pack(anchor="w", padx=15, pady=(3, 15))

        # Card 2: Backups
        self.backup_card = ctk.CTkFrame(self.right_container, fg_color="#1e1e1e", corner_radius=10)
        self.backup_card.pack(fill="x")
        
        self.backup_title = ctk.CTkLabel(
            self.backup_card, text="BACKUP & IMPORT", 
            font=ctk.CTkFont(size=14, weight="bold"), text_color="#1DB954"
        )
        self.backup_title.pack(anchor="w", padx=15, pady=(15, 10))
        
        self.export_btn = ctk.CTkButton(
            self.backup_card, text="Export JSON Backup", 
            fg_color="#1DB954", hover_color="#179b46", command=self.export_backup
        )
        self.export_btn.pack(fill="x", padx=15, pady=8)
        
        self.import_btn = ctk.CTkButton(
            self.backup_card, text="Import JSON Backup", 
            fg_color="#333333", hover_color="#444444", command=self.import_backup
        )
        self.import_btn.pack(fill="x", padx=15, pady=(8, 15))

        # Initial refresh
        self.refresh_all()

    def on_search_key(self, event):
        # Refresh on key release for real-time search filtering
        self.refresh_list()

    def refresh_all(self):
        self.refresh_list()
        self.refresh_metrics()

    def refresh_list(self):
        # Clear items
        for child in self.list_scroll.winfo_children():
            child.destroy()
            
        search_query = self.search_entry.get().strip()
        bookmarks = database.get_all_bookmarks(search_query)
        
        if not bookmarks:
            lbl = ctk.CTkLabel(self.list_scroll, text="No bookmarked songs found.", text_color="#888888")
            lbl.pack(expand=True, pady=100)
            return

        for bookmark in bookmarks:
            song = bookmark["song_name"]
            artist = bookmark["artist_name"]
            url = bookmark["bookmarked_url"]
            
            # Check if lyrics exist in database
            lyrics = database.get_lyrics(song, artist)
            has_lyrics = lyrics is not None and bool(lyrics.get("original_lyrics"))
            
            # Card card
            card = ctk.CTkFrame(self.list_scroll, fg_color="#1e1e1e", corner_radius=6, border_color="#2b2b2b", border_width=1)
            card.pack(fill="x", padx=5, pady=4)
            card.grid_columnconfigure(0, weight=1)
            card.grid_rowconfigure(0, weight=1)
            
            # Info container
            info_frame = ctk.CTkFrame(card, fg_color="transparent")
            info_frame.grid(row=0, column=0, padx=15, pady=8, sticky="w")
            
            # Title with checkmark status
            status_symbol = "✓" if has_lyrics else "✗"
            status_color = "#1DB954" if has_lyrics else "#ff4a4a"
            
            title_text = f"[{status_symbol}]  {song}"
            title_lbl = ctk.CTkLabel(
                info_frame, text=title_text, 
                font=ctk.CTkFont(size=14, weight="bold"), 
                text_color="#ffffff", anchor="w"
            )
            title_lbl.pack(anchor="w")
            
            artist_lbl = ctk.CTkLabel(
                info_frame, text=artist, 
                font=ctk.CTkFont(size=12), 
                text_color="#888888", anchor="w"
            )
            artist_lbl.pack(anchor="w")
            
            # Click card to load lyrics in Now Playing
            card.bind("<Button-1>", lambda event, s=song, a=artist, u=url: self.load_song_in_player(s, a, u))
            info_frame.bind("<Button-1>", lambda event, s=song, a=artist, u=url: self.load_song_in_player(s, a, u))
            title_lbl.bind("<Button-1>", lambda event, s=song, a=artist, u=url: self.load_song_in_player(s, a, u))
            artist_lbl.bind("<Button-1>", lambda event, s=song, a=artist, u=url: self.load_song_in_player(s, a, u))

            # Action buttons
            actions_frame = ctk.CTkFrame(card, fg_color="transparent")
            actions_frame.grid(row=0, column=1, padx=15, pady=8, sticky="e")
            
            play_btn = ctk.CTkButton(
                actions_frame, text="View", width=60, height=26, 
                fg_color="#333333", hover_color="#444444",
                command=lambda s=song, a=artist, u=url: self.load_song_in_player(s, a, u)
            )
            play_btn.pack(side="left", padx=5)
            
            delete_btn = ctk.CTkButton(
                actions_frame, text="Delete", width=60, height=26,
                fg_color="#552222", hover_color="#773333",
                command=lambda bid=bookmark["id"], s=song: self.delete_bookmark(bid, s)
            )
            delete_btn.pack(side="left", padx=5)

    def load_song_in_player(self, song, artist, url):
        # Notify controller to switch to Now Playing frame and start loading
        self.controller.load_cached_lyrics(song, artist, url)

    def delete_bookmark(self, bookmark_id, song_name):
        if messagebox.askyesno("Delete Bookmark", f"Are you sure you want to delete '{song_name}' and its cached lyrics?"):
            database.delete_bookmark_by_id(bookmark_id)
            self.refresh_all()

    def refresh_metrics(self):
        # Calculate library metrics
        bookmarks = database.get_all_bookmarks()
        total_songs = len(bookmarks)
        
        cached_count = 0
        total_size = 0
        for b in bookmarks:
            # Check lyrics size
            lyrics = database.get_lyrics(b["song_name"], b["artist_name"])
            if lyrics:
                if lyrics.get("original_lyrics"):
                    cached_count += 1
                total_size += len(lyrics.get("original_lyrics") or "")
                total_size += len(lyrics.get("translated_lyrics") or "")
                total_size += len(lyrics.get("ai_romanized") or "")
                total_size += len(lyrics.get("ai_translation") or "")
            total_size += len(b["song_name"] or "")
            total_size += len(b["artist_name"] or "")
            total_size += len(b["bookmarked_url"] or "")
            total_size += len(b["title"] or "")

        # Format database size
        size_kb = total_size / 1024
        size_mb = size_kb / 1024
        if size_mb >= 1.0:
            db_size_str = f"{size_mb:.2f} MB"
        else:
            db_size_str = f"{size_kb:.1f} KB"
            
        self.total_bookmarks_lbl.configure(text=f"Bookmarked Songs: {total_songs}")
        self.with_lyrics_lbl.configure(text=f"Cached Lyrics: {cached_count}")
        self.db_space_lbl.configure(text=f"Database Space: {db_size_str}")

    # --- Export / Import Backups ---
    def export_backup(self):
        file_path = filedialog.asksaveasfilename(
            title="Export SpotLyric Backup",
            defaultextension=".json",
            filetypes=[("JSON Files", "*.json")],
            initialfile="spotlyric_backup.json"
        )
        if not file_path:
            return
            
        res = backup.export_data(file_path)
        if res.get("success"):
            messagebox.showinfo("Backup Exported", res["message"])
        else:
            messagebox.showerror("Export Failed", f"Could not export data: {res.get('error')}")

    def import_backup(self):
        file_path = filedialog.askopenfilename(
            title="Import SpotLyric Backup",
            filetypes=[("JSON Files", "*.json")]
        )
        if not file_path:
            return
            
        if messagebox.askyesno("Import Backup", "Importing backup will restore your bookmarked songs and settings. Any conflicting bookmarks will be overwritten. Proceed?"):
            res = backup.import_data(file_path)
            if res.get("success"):
                messagebox.showinfo("Backup Imported", res["message"])
                self.refresh_all()
                # Refresh preferred sources and settings if open
                self.controller.refresh_other_tabs()
            else:
                messagebox.showerror("Import Failed", f"Could not import data: {res.get('error')}")
