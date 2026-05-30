from django.db import models


class BookmarkedSong(models.Model):
    """Minimal model to store bookmarked search results for songs"""

    song_name = models.CharField(max_length=255)
    artist_name = models.CharField(max_length=255, blank=True, null=True)
    bookmarked_url = models.URLField(max_length=1000)
    title = models.CharField(max_length=500, blank=True, null=True)

    class Meta:
        db_table = "bookmarked_song"
        unique_together = ("song_name", "artist_name")
        indexes = [
            models.Index(fields=["song_name"]),
            models.Index(fields=["song_name", "artist_name"]),
        ]

    @classmethod
    def get_bookmarked_url(cls, song_name, artist_name=None):
        """Get bookmarked URL for a song, return None if not found"""
        try:
            bookmark = cls.objects.get(song_name__iexact=song_name, artist_name__iexact=artist_name)
            return bookmark.bookmarked_url
        except cls.DoesNotExist:
            return None

    @classmethod
    def save_bookmark(cls, song_name, artist_name, url, title=None):
        """Save or update a bookmark for a song"""
        bookmark, created = cls.objects.update_or_create(
            song_name__iexact=song_name,
            artist_name__iexact=artist_name,
            defaults={
                "song_name": song_name,
                "artist_name": artist_name,
                "bookmarked_url": url,
                "title": title,
            },
        )
        return bookmark

    def __str__(self):
        if self.artist_name:
            return f"{self.song_name} - {self.artist_name}"
        return self.song_name


class SongLyrics(models.Model):
    """Minimal model to store lyrics linked to bookmarks"""

    bookmark = models.OneToOneField(BookmarkedSong, on_delete=models.CASCADE, related_name="lyrics")
    original_lyrics = models.TextField(blank=True)
    translated_lyrics = models.TextField(blank=True)
    ai_romanized = models.TextField(blank=True)
    ai_translation = models.TextField(blank=True)

    class Meta:
        db_table = "song_lyrics"

    @classmethod
    def get_lyrics(cls, song_name, artist_name):
        """Get lyrics for a song via bookmark lookup"""
        try:
            bookmark = BookmarkedSong.objects.get(
                song_name__iexact=song_name,
                artist_name__iexact=artist_name,
            )
            return getattr(bookmark, "lyrics", None)
        except BookmarkedSong.DoesNotExist:
            return None

    @classmethod
    def save_lyrics(
        cls,
        song_name,
        artist_name,
        original_lyrics=None,
        translated_lyrics=None,
        source_url=None,
        source_language=None,
        target_language=None,
        extraction_data=None,
    ):
        """Save lyrics for a song - creates bookmark if needed"""
        # Get or create bookmark
        bookmark, _ = BookmarkedSong.objects.get_or_create(
            song_name=song_name,
            artist_name=artist_name,
            defaults={
                "song_name": song_name,
                "artist_name": artist_name,
                "bookmarked_url": source_url or "",
            },
        )

        # Update bookmark URL if provided
        if source_url and bookmark.bookmarked_url != source_url:
            bookmark.bookmarked_url = source_url
            bookmark.save()

        # Create or update lyrics
        lyrics, created = cls.objects.update_or_create(
            bookmark=bookmark,
            defaults={
                "original_lyrics": original_lyrics or "",
                "translated_lyrics": translated_lyrics or "",
            },
        )
        return lyrics, created

    def has_lyrics(self):
        return bool(self.original_lyrics or self.translated_lyrics)

    @property
    def song_name(self):
        return self.bookmark.song_name

    @property
    def artist_name(self):
        return self.bookmark.artist_name

    @property
    def source_url(self):
        return self.bookmark.bookmarked_url

    def __str__(self):
        status = "✓" if self.has_lyrics() else "✗"
        return f"{status} {self.song_name} - {self.artist_name}"
