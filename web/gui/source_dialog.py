import threading
import customtkinter as ctk
from services import search

class SourceDialog(ctk.CTkToplevel):
    def __init__(self, parent, song_name, artist_name, callback):
        super().__init__(parent)
        self.title("Switch Lyrics Source")
        self.geometry("650x450")
        self.resizable(False, False)
        
        self.song_name = song_name
        self.artist_name = artist_name
        self.callback = callback
        
        # Grid layout
        self.grid_rowconfigure(1, weight=1)
        self.grid_columnconfigure(0, weight=1)
        
        # Header title
        self.header_label = ctk.CTkLabel(
            self, text=f"Lyrics sources for: {song_name} - {artist_name}",
            font=ctk.CTkFont(size=14, weight="bold")
        )
        self.header_label.grid(row=0, column=0, padx=15, pady=15, sticky="w")
        
        # Scrollable container for sources
        self.scroll_frame = ctk.CTkScrollableFrame(self, fg_color="#121212")
        self.scroll_frame.grid(row=1, column=0, padx=15, pady=(0, 15), sticky="nsew")
        
        # Loading indicator
        self.loading_label = ctk.CTkLabel(
            self.scroll_frame, text="Searching SerpAPI for lyrics sources...",
            font=ctk.CTkFont(size=14), text_color="#1DB954"
        )
        self.loading_label.pack(expand=True, pady=100)
        
        # Start search thread
        threading.Thread(target=self.load_sources, daemon=True).start()

    def load_sources(self):
        try:
            # Query with SerpAPI
            results = search.search_lyrics_sources(self.song_name, self.artist_name, translation_only=True)
            self.after(0, self.display_sources, results)
        except Exception as e:
            self.after(0, self.display_error, str(e))

    def display_error(self, err):
        for child in self.scroll_frame.winfo_children():
            child.destroy()
        lbl = ctk.CTkLabel(self.scroll_frame, text=f"Search failed: {err}", text_color="#ff4a4a")
        lbl.pack(expand=True, pady=100)

    def display_sources(self, results):
        # Clear loading label
        for child in self.scroll_frame.winfo_children():
            child.destroy()
            
        if not results:
            lbl = ctk.CTkLabel(self.scroll_frame, text="No search results found.", text_color="#888888")
            lbl.pack(expand=True, pady=100)
            return

        for idx, item in enumerate(results):
            # Outer frame card
            border_color = "#1DB954" if item.get("is_preferred") else "#2b2b2b"
            card = ctk.CTkFrame(self.scroll_frame, fg_color="#1e1e1e", border_color=border_color, border_width=1, corner_radius=6)
            card.pack(fill="x", padx=5, pady=5)
            
            card.grid_columnconfigure(0, weight=1)
            card.grid_rowconfigure(0, weight=1)
            
            # Text layout inside card
            text_frame = ctk.CTkFrame(card, fg_color="transparent")
            text_frame.grid(row=0, column=0, padx=15, pady=10, sticky="w")
            
            # Title
            title_text = item["title"]
            if item.get("is_preferred"):
                title_text = "★ " + title_text
            title_lbl = ctk.CTkLabel(text_frame, text=title_text, font=ctk.CTkFont(size=14, weight="bold"), anchor="w")
            title_lbl.pack(anchor="w")
            
            # URL
            url_lbl = ctk.CTkLabel(text_frame, text=item["url"], font=ctk.CTkFont(size=12), text_color="#888888", anchor="w")
            url_lbl.pack(anchor="w")
            
            # Snippet & score (metrics)
            score = item.get("score", 0.0)
            metric_text = f"Composite Score: {score:.2f}"
            if item.get("is_preferred"):
                metric_text += " (Preferred Source Boost +2.0)"
            
            metric_lbl = ctk.CTkLabel(
                text_frame, text=metric_text,
                font=ctk.CTkFont(size=11), text_color="#1DB954", anchor="w"
            )
            metric_lbl.pack(anchor="w")

            # Selection button on the right
            btn = ctk.CTkButton(
                card, text="Select & Extract", width=120, height=28,
                fg_color="#1DB954", hover_color="#179b46",
                command=lambda url=item["url"]: self.select_url(url)
            )
            btn.grid(row=0, column=1, padx=15, pady=10, sticky="e")

    def select_url(self, url):
        self.callback(url)
        self.destroy()
