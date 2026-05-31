import os
import threading
import customtkinter as ctk
from tkinter import messagebox
from services import database, spotify

class SettingsFrame(ctk.CTkFrame):
    def __init__(self, parent, controller):
        super().__init__(parent)
        self.controller = controller
        
        # Grid layout: Left side (credentials form), Right side (API limits stats & Spotify OAuth card)
        self.grid_columnconfigure(0, weight=2)
        self.grid_columnconfigure(1, weight=1)
        self.grid_rowconfigure(0, weight=1)
        
        # Scrollable container on left for settings fields
        self.left_scroll = ctk.CTkScrollableFrame(self, fg_color="transparent")
        self.left_scroll.grid(row=0, column=0, padx=15, pady=15, sticky="nsew")
        self.left_scroll.columnconfigure(0, weight=1)
        
        # --- Left Column Settings Fields ---
        self.setup_settings_fields()
        
        # --- Right Column: Stats & Auth ---
        self.right_container = ctk.CTkFrame(self, fg_color="transparent")
        self.right_container.grid(row=0, column=1, padx=15, pady=15, sticky="nsew")
        
        # Card 1: Spotify OAuth
        self.spotify_card = ctk.CTkFrame(self.right_container, fg_color="#1e1e1e", corner_radius=10)
        self.spotify_card.pack(fill="x", pady=(0, 15))
        
        self.setup_spotify_auth_card()
        
        # Card 2: Request Counters
        self.stats_card = ctk.CTkFrame(self.right_container, fg_color="#1e1e1e", corner_radius=10)
        self.stats_card.pack(fill="x")
        
        self.setup_stats_card()
        
        # Load saved settings values into fields
        self.load_settings()

    def setup_settings_fields(self):
        # 1. Spotify Credentials section
        self.spotify_sec = ctk.CTkFrame(self.left_scroll, fg_color="#1e1e1e", corner_radius=8)
        self.spotify_sec.pack(fill="x", pady=(0, 15), padx=5)
        
        lbl_spot_title = ctk.CTkLabel(self.spotify_sec, text="SPOTIFY DEVELOPER CREDENTIALS", font=ctk.CTkFont(size=13, weight="bold"), text_color="#1DB954")
        lbl_spot_title.pack(anchor="w", padx=15, pady=(15, 10))
        
        # Client ID
        lbl_cid = ctk.CTkLabel(self.spotify_sec, text="Spotify Client ID:")
        lbl_cid.pack(anchor="w", padx=15)
        self.cid_entry = ctk.CTkEntry(self.spotify_sec, placeholder_text="Enter Spotify Client ID")
        self.cid_entry.pack(fill="x", padx=15, pady=(0, 10))
        
        # Client Secret
        lbl_sec = ctk.CTkLabel(self.spotify_sec, text="Spotify Client Secret:")
        lbl_sec.pack(anchor="w", padx=15)
        self.secret_entry = ctk.CTkEntry(self.spotify_sec, placeholder_text="Enter Spotify Client Secret", show="*")
        self.secret_entry.pack(fill="x", padx=15, pady=(0, 10))
        
        # Redirect URI
        lbl_ruri = ctk.CTkLabel(self.spotify_sec, text="Redirect URI:")
        lbl_ruri.pack(anchor="w", padx=15)
        self.redirect_entry = ctk.CTkEntry(self.spotify_sec, placeholder_text="http://localhost:8888/callback")
        self.redirect_entry.pack(fill="x", padx=15, pady=(0, 15))

        # 2. SerpAPI Credentials
        self.serp_sec = ctk.CTkFrame(self.left_scroll, fg_color="#1e1e1e", corner_radius=8)
        self.serp_sec.pack(fill="x", pady=(0, 15), padx=5)
        
        lbl_serp_title = ctk.CTkLabel(self.serp_sec, text="SERPAPI CREDENTIALS (GOOGLE SEARCH)", font=ctk.CTkFont(size=13, weight="bold"), text_color="#1DB954")
        lbl_serp_title.pack(anchor="w", padx=15, pady=(15, 10))
        
        lbl_skey = ctk.CTkLabel(self.serp_sec, text="SerpAPI Key:")
        lbl_skey.pack(anchor="w", padx=15)
        self.serpkey_entry = ctk.CTkEntry(self.serp_sec, placeholder_text="Enter SerpAPI Key", show="*")
        self.serpkey_entry.pack(fill="x", padx=15, pady=(0, 15))

        # 3. Gemini Credentials
        self.gemini_sec = ctk.CTkFrame(self.left_scroll, fg_color="#1e1e1e", corner_radius=8)
        self.gemini_sec.pack(fill="x", pady=(0, 15), padx=5)
        
        lbl_gem_title = ctk.CTkLabel(self.gemini_sec, text="GEMINI AI CREDENTIALS", font=ctk.CTkFont(size=13, weight="bold"), text_color="#1DB954")
        lbl_gem_title.pack(anchor="w", padx=15, pady=(15, 10))
        
        lbl_gkey = ctk.CTkLabel(self.gemini_sec, text="Gemini API Key:")
        lbl_gkey.pack(anchor="w", padx=15)
        self.gemkey_entry = ctk.CTkEntry(self.gemini_sec, placeholder_text="Enter Gemini API Key", show="*")
        self.gemkey_entry.pack(fill="x", padx=15, pady=(0, 10))
        
        lbl_gmodel = ctk.CTkLabel(self.gemini_sec, text="Gemini Model:")
        lbl_gmodel.pack(anchor="w", padx=15)
        self.model_menu = ctk.CTkComboBox(
            self.gemini_sec, values=["gemini-2.5-flash-lite", "gemini-1.5-flash", "gemini-1.5-pro", "gemini-2.0-flash"]
        )
        self.model_menu.pack(fill="x", padx=15, pady=(0, 15))

        # 4. Relevance Filter Settings
        self.filter_sec = ctk.CTkFrame(self.left_scroll, fg_color="#1e1e1e", corner_radius=8)
        self.filter_sec.pack(fill="x", pady=(0, 15), padx=5)
        
        lbl_filt_title = ctk.CTkLabel(self.filter_sec, text="RELEVANCE FILTERING (SERP RESULTS)", font=ctk.CTkFont(size=13, weight="bold"), text_color="#1DB954")
        lbl_filt_title.pack(anchor="w", padx=15, pady=(15, 10))
        
        self.filt_toggle = ctk.CTkSwitch(self.filter_sec, text="Enable Relevance Filter")
        self.filt_toggle.pack(anchor="w", padx=15, pady=(0, 10))
        
        self.slider_label = ctk.CTkLabel(self.filter_sec, text="Relevance Threshold: 0.20", font=ctk.CTkFont(size=12))
        self.slider_label.pack(anchor="w", padx=15)
        
        self.threshold_slider = ctk.CTkSlider(
            self.filter_sec, from_=0.0, to=1.0, number_of_steps=100, 
            command=self.update_slider_label
        )
        self.threshold_slider.pack(fill="x", padx=15, pady=(0, 15))

        # Save Button
        self.save_btn = ctk.CTkButton(
            self.left_scroll, text="Save Settings", height=38,
            fg_color="#1DB954", hover_color="#179b46", font=ctk.CTkFont(size=14, weight="bold"),
            command=self.save_settings
        )
        self.save_btn.pack(fill="x", pady=5, padx=5)

    def setup_spotify_auth_card(self):
        title = ctk.CTkLabel(
            self.spotify_card, text="SPOTIFY SYNC CONTROL", 
            font=ctk.CTkFont(size=14, weight="bold"), text_color="#1DB954"
        )
        title.pack(anchor="w", padx=15, pady=(15, 10))
        
        desc = (
            "1. Enter Spotify Client ID/Secret on left.\n"
            "2. Click the authorization button below.\n"
            "3. Allow access in the browser page.\n"
            "4. The app will sync your song playback automatically."
        )
        desc_lbl = ctk.CTkLabel(
            self.spotify_card, text=desc, font=ctk.CTkFont(size=11), 
            wraplength=230, justify="left", anchor="w"
        )
        desc_lbl.pack(anchor="w", padx=15, pady=5)
        
        self.auth_status_lbl = ctk.CTkLabel(
            self.spotify_card, text="Status: Disconnected", 
            font=ctk.CTkFont(size=12, weight="bold"), text_color="#ff4a4a"
        )
        self.auth_status_lbl.pack(anchor="w", padx=15, pady=5)
        
        self.auth_btn = ctk.CTkButton(
            self.spotify_card, text="Authorize Spotify", 
            fg_color="#1DB954", hover_color="#179b46",
            command=self.trigger_spotify_auth
        )
        self.auth_btn.pack(fill="x", padx=15, pady=(10, 15))

    def setup_stats_card(self):
        title = ctk.CTkLabel(
            self.stats_card, text="MONTHLY API USAGE", 
            font=ctk.CTkFont(size=14, weight="bold"), text_color="#1DB954"
        )
        title.pack(anchor="w", padx=15, pady=(15, 10))
        
        self.spot_req_lbl = ctk.CTkLabel(self.stats_card, text="Spotify Requests: --", font=ctk.CTkFont(size=13))
        self.spot_req_lbl.pack(anchor="w", padx=15, pady=4)
        
        self.serp_req_lbl = ctk.CTkLabel(self.stats_card, text="SerpAPI Requests: -- (Limit 100/mo)", font=ctk.CTkFont(size=13))
        self.serp_req_lbl.pack(anchor="w", padx=15, pady=4)
        
        self.gem_req_lbl = ctk.CTkLabel(self.stats_card, text="Gemini AI Requests: --", font=ctk.CTkFont(size=13))
        self.gem_req_lbl.pack(anchor="w", padx=15, pady=(4, 15))

        self.refresh_stats_btn = ctk.CTkButton(
            self.stats_card, text="Refresh Counters", width=120, height=24,
            fg_color="#333333", hover_color="#444444",
            command=self.refresh_stats
        )
        self.refresh_stats_btn.pack(padx=15, pady=(0, 15))

    def update_slider_label(self, val):
        self.slider_label.configure(text=f"Relevance Threshold: {val:.2f}")

    # --- Load & Save Settings ---
    def load_settings(self):
        # API Keys & Custom Model
        self.cid_entry.insert(0, database.get_setting("spotify_custom_client_id", ""))
        self.secret_entry.insert(0, database.get_setting("spotify_custom_client_secret", ""))
        self.redirect_entry.insert(0, database.get_setting("spotify_custom_redirect_uri", "http://localhost:8888/callback"))
        
        self.serpkey_entry.insert(0, database.get_setting("serpapi_custom_api_key", ""))
        self.gemkey_entry.insert(0, database.get_setting("gemini_custom_api_key", ""))
        
        model_name = database.get_setting("gemini_custom_model", "gemini-2.5-flash-lite")
        self.model_menu.set(model_name)

        # Relevance Filter
        enabled = int(database.get_setting("relevance_filter_enabled", 1)) == 1
        if enabled:
            self.filt_toggle.select()
        else:
            self.filt_toggle.deselect()
            
        threshold = float(database.get_setting("relevance_threshold", 0.2))
        self.threshold_slider.set(threshold)
        self.update_slider_label(threshold)

        # Refresh stats counters & connection status
        self.refresh_stats()
        self.check_auth_status()

    def save_settings(self):
        database.set_setting("spotify_custom_client_id", self.cid_entry.get().strip())
        database.set_setting("spotify_custom_client_secret", self.secret_entry.get().strip())
        database.set_setting("spotify_custom_redirect_uri", self.redirect_entry.get().strip())
        
        database.set_setting("serpapi_custom_api_key", self.serpkey_entry.get().strip())
        database.set_setting("gemini_custom_api_key", self.gemkey_entry.get().strip())
        database.set_setting("gemini_custom_model", self.model_menu.get().strip())
        
        database.set_setting("relevance_filter_enabled", 1 if self.filt_toggle.get() else 0)
        database.set_setting("relevance_threshold", self.threshold_slider.get())
        
        messagebox.showinfo("Settings Saved", "Your settings have been saved successfully.")
        self.check_auth_status()

    def check_auth_status(self):
        if spotify.is_authorized():
            self.auth_status_lbl.configure(text="Status: Connected", text_color="#1DB954")
            self.auth_btn.configure(text="Re-Authorize Spotify")
        else:
            self.auth_status_lbl.configure(text="Status: Disconnected", text_color="#ff4a4a")
            self.auth_btn.configure(text="Authorize Spotify")

    # --- Spotify Auth Trigger ---
    def trigger_spotify_auth(self):
        # Make sure settings are saved first
        cid = self.cid_entry.get().strip()
        secret = self.secret_entry.get().strip()
        
        if not cid or not secret:
            messagebox.showwarning("Missing Credentials", "Please enter your Spotify Client ID and Client Secret on the left before authorizing.")
            return

        # Save first to make sure oauth manager can pick them up
        database.set_setting("spotify_custom_client_id", cid)
        database.set_setting("spotify_custom_client_secret", secret)
        database.set_setting("spotify_custom_redirect_uri", self.redirect_entry.get().strip())
        
        self.auth_status_lbl.configure(text="Status: Authenticating...", text_color="#ffd43b")
        
        # Run authorization server in a separate thread so it doesn't freeze the GUI!
        def run_auth():
            try:
                oauth = spotify.get_oauth_manager()
                if oauth:
                    # This call will open the default web browser and start the local HTTP server 
                    # on port 8888 (or whichever port redirect_uri specifies) to capture the callback redirect.
                    token_info = oauth.get_access_token(as_dict=False)
                    if token_info:
                        self.after(0, lambda: messagebox.showinfo("Success", "Spotify authorization completed successfully!"))
                        self.after(0, self.check_auth_status)
                        return
                self.after(0, lambda: messagebox.showerror("Error", "Spotify authorization failed. Verify client credentials and redirect URI."))
                self.after(0, self.check_auth_status)
            except Exception as e:
                self.after(0, lambda: messagebox.showerror("Auth Exception", str(e)))
                self.after(0, self.check_auth_status)

        threading.Thread(target=run_auth, daemon=True).start()

    def refresh_stats(self):
        counts = database.get_request_counts()
        self.spot_req_lbl.configure(text=f"Spotify Requests: {counts['spotify']}")
        self.serp_req_lbl.configure(text=f"SerpAPI Requests: {counts['serpapi']} (Limit 100/mo)")
        self.gem_req_lbl.configure(text=f"Gemini AI Requests: {counts['gemini']}")
