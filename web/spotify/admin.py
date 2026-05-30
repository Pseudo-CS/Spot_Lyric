from django.contrib import admin
from django.utils.html import format_html
from .models import BookmarkedSong, SongLyrics


@admin.register(BookmarkedSong)
class BookmarkedSongAdmin(admin.ModelAdmin):
    list_display = ("song_name", "artist_name", "title", "bookmarked_url")
    search_fields = ("song_name", "artist_name", "title", "bookmarked_url")
    ordering = ("song_name",)

    fieldsets = (
        ("Song Information", {"fields": ("song_name", "artist_name")}),
        ("Bookmark Details", {"fields": ("bookmarked_url", "title")}),
    )


@admin.register(SongLyrics)
class SongLyricsAdmin(admin.ModelAdmin):
    list_display = ("status_icon", "song_name", "artist_name", "has_original", "has_translation")
    search_fields = ("bookmark__song_name", "bookmark__artist_name", "original_lyrics", "translated_lyrics")
    ordering = ("bookmark__song_name",)

    fieldsets = (
        ("Song Information", {"fields": ("bookmark",)}),
        ("Lyrics Content", {"fields": ("original_lyrics", "translated_lyrics")}),
    )

    def status_icon(self, obj):
        if obj.has_lyrics():
            return format_html('<span style="color: green;">✓</span>')
        return format_html('<span style="color: red;">✗</span>')

    status_icon.short_description = "Status"

    def song_name(self, obj):
        return obj.bookmark.song_name

    song_name.short_description = "Song Name"
    song_name.admin_order_field = "bookmark__song_name"

    def artist_name(self, obj):
        return obj.bookmark.artist_name

    artist_name.short_description = "Artist Name"
    artist_name.admin_order_field = "bookmark__artist_name"

    def has_original(self, obj):
        return bool(obj.original_lyrics)

    has_original.boolean = True
    has_original.short_description = "Original"

    def has_translation(self, obj):
        return bool(obj.translated_lyrics)

    has_translation.boolean = True
    has_translation.short_description = "Translation"

    actions = ["reextract_lyrics"]

    def reextract_lyrics(self, request, queryset):
        """Admin action to re-extract lyrics for selected songs"""
        from .gemini_utils import process_bookmarked_page_for_lyrics

        updated_count = 0
        for lyrics_obj in queryset:
            if lyrics_obj.bookmark.bookmarked_url:
                try:
                    result = process_bookmarked_page_for_lyrics(
                        lyrics_obj.bookmark.bookmarked_url,
                        lyrics_obj.bookmark.song_name,
                        lyrics_obj.bookmark.artist_name,
                    )

                    if result.get("success", False):
                        lyrics_obj.original_lyrics = result.get("original_lyrics", "")
                        lyrics_obj.translated_lyrics = result.get("translated_lyrics", "")
                        lyrics_obj.save()
                        updated_count += 1
                except Exception as e:
                    self.message_user(
                        request,
                        f"Error re-extracting lyrics for {lyrics_obj}: {e}",
                        level="ERROR",
                    )

        self.message_user(request, f"Successfully re-extracted lyrics for {updated_count} songs.")

    reextract_lyrics.short_description = "Re-extract lyrics from source URLs"
