"""
Django management command to extract lyrics for all existing bookmarked songs.
Usage: python manage.py extract_lyrics_bulk [--limit N] [--skip-existing]
"""

import time
from django.core.management.base import BaseCommand
from spotify.models import BookmarkedSong, SongLyrics
from spotify.gemini_utils import extract_lyrics_with_gemini
import requests
from bs4 import BeautifulSoup


class Command(BaseCommand):
    help = "Extract lyrics for all existing bookmarked songs using Gemini AI"

    def add_arguments(self, parser):
        parser.add_argument(
            "--limit",
            type=int,
            help="Limit the number of songs to process",
        )
        parser.add_argument(
            "--skip-existing",
            action="store_true",
            help="Skip songs that already have lyrics extracted",
        )
        parser.add_argument(
            "--delay",
            type=float,
            default=2.0,
            help="Delay between API calls in seconds (default: 2.0)",
        )
        parser.add_argument(
            "--dry-run",
            action="store_true",
            help="Show what would be processed without actually extracting lyrics",
        )

    def handle(self, *args, **options):
        self.stdout.write(self.style.SUCCESS("Starting bulk lyrics extraction..."))

        queryset = BookmarkedSong.objects.all()

        if options["skip_existing"]:
            from django.db.models import Q

            queryset = queryset.exclude(
                Q(lyrics__isnull=False)
                & (Q(lyrics__original_lyrics__gt="") | Q(lyrics__translated_lyrics__gt=""))
            )
            self.stdout.write("Skipping songs with existing lyrics content...")

        if options["limit"]:
            queryset = queryset[: options["limit"]]

        total_songs = queryset.count()

        if total_songs == 0:
            self.stdout.write(self.style.WARNING("No songs found to process."))
            return

        self.stdout.write(f"Found {total_songs} songs to process")

        if options["dry_run"]:
            self.stdout.write(self.style.WARNING("DRY RUN MODE - No lyrics will be extracted"))
            for song in queryset:
                self.stdout.write(f"Would process: {song.song_name} by {song.artist_name}")
                if song.bookmarked_url:
                    self.stdout.write(f"  URL: {song.bookmarked_url}")
            return

        success_count = 0
        error_count = 0
        skipped_count = 0

        for i, song in enumerate(queryset, 1):
            self.stdout.write(f"\n[{i}/{total_songs}] Processing: {song.song_name} by {song.artist_name}")

            try:
                try:
                    if hasattr(song, "lyrics") and song.lyrics.has_lyrics():
                        self.stdout.write(self.style.WARNING("  Lyrics with content already exist, skipping..."))
                        skipped_count += 1
                        continue
                except Exception:
                    pass

                if not song.bookmarked_url:
                    self.stdout.write(self.style.ERROR("  No bookmark URL found, skipping..."))
                    skipped_count += 1
                    continue

                result = self.extract_lyrics_for_song(song)

                if result:
                    success_count += 1
                    self.stdout.write(self.style.SUCCESS("  ✓ Lyrics extracted and saved successfully"))
                else:
                    error_count += 1
                    self.stdout.write(self.style.ERROR("  ✗ Failed to extract lyrics"))

                if i < total_songs:
                    time.sleep(options["delay"])

            except Exception as e:
                error_count += 1
                self.stdout.write(self.style.ERROR(f"  ✗ Error processing song: {str(e)}"))

        self.stdout.write(self.style.SUCCESS("\n" + "=" * 50))
        self.stdout.write(self.style.SUCCESS("BULK EXTRACTION COMPLETE"))
        self.stdout.write(f"Total processed: {total_songs}")
        self.stdout.write(self.style.SUCCESS(f"Successful: {success_count}"))
        self.stdout.write(self.style.ERROR(f"Failed: {error_count}"))
        self.stdout.write(self.style.WARNING(f"Skipped: {skipped_count}"))

    def extract_lyrics_for_song(self, song):
        """Extract lyrics for a single song"""
        try:
            self.stdout.write(f"  Fetching content from: {song.bookmarked_url}")

            headers = {
                "User-Agent": (
                    "Mozilla/5.0 (Windows NT 10.0; Win64; x64) "
                    "AppleWebKit/537.36 (KHTML, like Gecko) "
                    "Chrome/91.0.4472.124 Safari/537.36"
                )
            }

            response = requests.get(song.bookmarked_url, headers=headers, timeout=10)
            response.raise_for_status()

            soup = BeautifulSoup(response.text, "html.parser")

            for script in soup(["script", "style"]):
                script.extract()

            page_content = soup.get_text()

            lines = (line.strip() for line in page_content.splitlines())
            page_content = "\n".join(line for line in lines if line)

            self.stdout.write(f"  Extracted {len(page_content)} characters from webpage")
            self.stdout.write("  Sending to Gemini AI for lyrics extraction...")

            lyrics_data = extract_lyrics_with_gemini(
                page_content,
                song.song_name,
                song.artist_name,
            )

            if not lyrics_data:
                return False

            self.stdout.write(f"  Lyrics data success: {lyrics_data.get('success', False)}")
            original = lyrics_data.get("original_lyrics", "")
            translated = lyrics_data.get("translated_lyrics", "")
            self.stdout.write(f"  Original lyrics length: {len(original)}")
            self.stdout.write(f"  Translated lyrics length: {len(translated)}")

            if len(original) > 100:
                self.stdout.write(f"  Original preview: {original[:100]}...")
            if len(translated) > 100:
                self.stdout.write(f"  Translated preview: {translated[:100]}...")

            song_lyrics, created = SongLyrics.save_lyrics(
                song_name=song.song_name,
                artist_name=song.artist_name,
                original_lyrics=original,
                translated_lyrics=translated,
                source_url=song.bookmarked_url,
            )

            song_lyrics.refresh_from_db()
            self.stdout.write(f"  Saved original length: {len(song_lyrics.original_lyrics)}")
            self.stdout.write(f"  Saved translated length: {len(song_lyrics.translated_lyrics)}")

            if created:
                self.stdout.write(f"  Created new SongLyrics record with ID: {song_lyrics.id}")
            else:
                self.stdout.write(f"  Updated existing SongLyrics record with ID: {song_lyrics.id}")
            return True

        except requests.exceptions.RequestException as e:
            self.stdout.write(self.style.ERROR(f"  Network error: {str(e)}"))
            return False
        except Exception as e:
            self.stdout.write(self.style.ERROR(f"  Unexpected error: {str(e)}"))
            return False
