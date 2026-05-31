import customtkinter as ctk
from tkinter import messagebox
from services import database

class SourcesFrame(ctk.CTkFrame):
    def __init__(self, parent, controller):
        super().__init__(parent)
        self.controller = controller
        
        # Grid layout: Left side (sources list & add form), Right side (tier scoring overview info)
        self.grid_columnconfigure(0, weight=3)
        self.grid_columnconfigure(1, weight=2)
        self.grid_rowconfigure(0, weight=1)
        
        # --- Left Column: Sources List & Add Form ---
        self.left_container = ctk.CTkFrame(self, fg_color="transparent")
        self.left_container.grid(row=0, column=0, padx=15, pady=15, sticky="nsew")
        
        self.left_container.grid_rowconfigure(1, weight=1)
        self.left_container.grid_columnconfigure(0, weight=1)
        
        # Form to add a new domain source
        self.add_form = ctk.CTkFrame(self.left_container, fg_color="#1e1e1e", corner_radius=8)
        self.add_form.grid(row=0, column=0, pady=(0, 15), sticky="ew")
        
        self.form_title = ctk.CTkLabel(
            self.add_form, text="ADD PREFERRED DOMAIN", 
            font=ctk.CTkFont(size=13, weight="bold"), text_color="#1DB954"
        )
        self.form_title.pack(anchor="w", padx=15, pady=(12, 8))
        
        self.inputs_frame = ctk.CTkFrame(self.add_form, fg_color="transparent")
        self.inputs_frame.pack(fill="x", padx=15, pady=(0, 12))
        
        self.domain_entry = ctk.CTkEntry(self.inputs_frame, placeholder_text="domain.com (e.g. genius.com)")
        self.domain_entry.pack(side="left", fill="x", expand=True, padx=(0, 10))
        
        self.name_entry = ctk.CTkEntry(self.inputs_frame, placeholder_text="Display Name (e.g. Genius)")
        self.name_entry.pack(side="left", fill="x", expand=True, padx=(0, 10))
        
        self.add_btn = ctk.CTkButton(
            self.inputs_frame, text="Add Domain", width=100, 
            fg_color="#1DB954", hover_color="#179b46", command=self.add_source
        )
        self.add_btn.pack(side="right")
        
        # Scrollable list of domains
        self.list_scroll = ctk.CTkScrollableFrame(self.left_container, fg_color="#121212")
        self.list_scroll.grid(row=1, column=0, sticky="nsew")
        
        # --- Right Column: Scoring Info ---
        self.right_container = ctk.CTkFrame(self, fg_color="#1e1e1e", corner_radius=10)
        self.right_container.grid(row=0, column=1, padx=15, pady=15, sticky="nsew")
        
        self.setup_scoring_info()

        # Initial list refresh
        self.refresh_list()

    def setup_scoring_info(self):
        title = ctk.CTkLabel(
            self.right_container, text="DOMAIN RANKING METRICS", 
            font=ctk.CTkFont(size=14, weight="bold"), text_color="#1DB954"
        )
        title.pack(anchor="w", padx=15, pady=(15, 10))
        
        desc = (
            "SpotLyric uses SerpAPI to fetch search results. The results are "
            "ranked using a composite score based on domain reliability and user preference:\n"
        )
        desc_lbl = ctk.CTkLabel(
            self.right_container, text=desc, font=ctk.CTkFont(size=12), 
            wraplength=230, justify="left", anchor="w"
        )
        desc_lbl.pack(anchor="w", padx=15, pady=5)
        
        # Tiers list
        tiers = [
            ("★ Preferred Domains", "+2.0 boost", "Custom domains added by you on the left. Floated to the top."),
            ("Tier 1 (Reliable)", "+1.0 score", "genius.com, lyricsraag.com, musixmatch.com"),
            ("Tier 2 (General)", "+0.5 score", "azlyrics.com, lyricfinder.org, letrastraducidas.org"),
            ("Tier 3 (Unreliable)", "-0.5 score", "songlyrics.com, metrolyrics.com, lyrics007.com"),
            ("Other sites", "+0.0 score", "Standard search matching score.")
        ]
        
        for name, boost, domains in tiers:
            t_frame = ctk.CTkFrame(self.right_container, fg_color="transparent")
            t_frame.pack(fill="x", padx=15, pady=6)
            
            lbl_name = ctk.CTkLabel(t_frame, text=name, font=ctk.CTkFont(size=12, weight="bold"), anchor="w")
            lbl_name.pack(anchor="w")
            
            lbl_boost = ctk.CTkLabel(t_frame, text=f"Ranking modifier: {boost}", font=ctk.CTkFont(size=11, slant="italic"), text_color="#1DB954", anchor="w")
            lbl_boost.pack(anchor="w")
            
            lbl_dom = ctk.CTkLabel(t_frame, text=domains, font=ctk.CTkFont(size=11), text_color="#888888", wraplength=230, justify="left", anchor="w")
            lbl_dom.pack(anchor="w")

    def refresh_list(self):
        # Clear items
        for child in self.list_scroll.winfo_children():
            child.destroy()
            
        sources = database.get_all_preferred_sources()
        if not sources:
            lbl = ctk.CTkLabel(self.list_scroll, text="No preferred domains configured.", text_color="#888888")
            lbl.pack(expand=True, pady=80)
            return

        for source in sources:
            domain = source["domain"]
            name = source["display_name"]
            enabled = bool(source["enabled"])
            
            card = ctk.CTkFrame(self.list_scroll, fg_color="#1e1e1e", corner_radius=6, border_color="#2b2b2b", border_width=1)
            card.pack(fill="x", padx=5, pady=4)
            card.grid_columnconfigure(0, weight=1)
            card.grid_rowconfigure(0, weight=1)
            
            # Label
            info_lbl = ctk.CTkLabel(
                card, text=f"{name} ({domain})", 
                font=ctk.CTkFont(size=13, weight="bold"), 
                anchor="w"
            )
            info_lbl.grid(row=0, column=0, padx=15, pady=8, sticky="w")
            
            # Right side controls
            ctrl_frame = ctk.CTkFrame(card, fg_color="transparent")
            ctrl_frame.grid(row=0, column=1, padx=15, pady=8, sticky="e")
            
            # Toggle Switch
            switch_var = ctk.BooleanVar(value=enabled)
            switch = ctk.CTkSwitch(
                ctrl_frame, text="Enabled", var=switch_var,
                command=lambda d=domain, v=switch_var: self.toggle_source(d, v.get())
            )
            switch.pack(side="left", padx=10)
            
            # Delete Button
            del_btn = ctk.CTkButton(
                ctrl_frame, text="Remove", width=60, height=24,
                fg_color="#552222", hover_color="#773333",
                command=lambda d=domain: self.delete_source(d)
            )
            del_btn.pack(side="left")

    def toggle_source(self, domain, is_enabled):
        database.update_preferred_source(domain, is_enabled)

    def delete_source(self, domain):
        database.delete_preferred_source(domain)
        self.refresh_list()

    def add_source(self):
        domain = self.domain_entry.get().strip().lower()
        name = self.name_entry.get().strip()
        
        if not domain or not name:
            messagebox.showwarning("Missing Fields", "Please enter both a Domain (e.g. genius.com) and a Display Name.")
            return
            
        # Basic validation: check if domain has dot
        if "." not in domain or len(domain) < 4:
            messagebox.showwarning("Invalid Domain", "Please enter a valid domain name (e.g., genius.com).")
            return
            
        database.add_preferred_source(domain, name, enabled=1)
        
        # Clear fields
        self.domain_entry.delete(0, "end")
        self.name_entry.delete(0, "end")
        
        self.refresh_list()
