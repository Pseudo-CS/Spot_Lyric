import os
import json
from datetime import datetime
from django.core.management.base import BaseCommand
from spotify.models import SpotifyCache, SpotifyBookmark


class Command(BaseCommand):
    help = "Migrate existing JSON cache data to database"

    def add_arguments(self, parser):
        parser.add_argument(
            "--json-file",
            type=str,
            default="search_cache.json",
            help="Path to the JSON cache file (default: search_cache.json)",
        )

    def handle(self, *args, **options):
        json_file = options["json_file"]

        if not os.path.exists(json_file):
            self.stdout.write(
                self.style.WARNING(f'JSON cache file "{json_file}" not found. Skipping migration.')
            )
            return

        try:
            with open(json_file, "r") as f:
                cache_data = json.load(f)

            migrated_count = 0
            error_count = 0

            for cache_key, (results, timestamp_str, bookmarks) in cache_data.items():
                try:
                    # Parse cache key to get song and artist names
                    if "_" not in cache_key:
                        self.stdout.write(
                            self.style.WARNING(f"Skipping invalid cache key: {cache_key}")
                        )
                        continue

                    # Split cache key back to song and artist (this is a best-effort approach)
                    parts = cache_key.split("_")
                    if len(parts) < 2:
                        continue

                    # Try to reconstruct song and artist names
                    # This is imperfect since the original names may have contained underscores
                    song_name = " ".join(parts[:-1]).title()
                    artist_name = parts[-1].title()

                    # Create or get cache entry
                    cache_entry, created = SpotifyCache.objects.get_or_create(
                        cache_key=cache_key,
                        defaults={
                            "song_name": song_name,
                            "artist_name": artist_name,
                        },
                    )

                    if created:
                        # Parse timestamp
                        try:
                            timestamp = datetime.fromisoformat(timestamp_str)
                            cache_entry.created_at = timestamp
                            cache_entry.updated_at = timestamp
                            cache_entry.save()
                        except ValueError:
                            pass  # Use default timestamps

                    # Migrate bookmarks
                    for url, meta in bookmarks.items():
                        title = None
                        if isinstance(meta, dict):
                            title = meta.get("title")
                        elif not isinstance(meta, bool):
                            title = str(meta)

                        SpotifyBookmark.objects.get_or_create(
                            cache=cache_entry,
                            url=url,
                            defaults={"title": title},
                        )

                    migrated_count += 1

                except Exception as e:
                    error_count += 1
                    self.stdout.write(
                        self.style.ERROR(f"Error migrating cache key {cache_key}: {str(e)}")
                    )

            self.stdout.write(
                self.style.SUCCESS(
                    f"Migration completed. Migrated {migrated_count} cache entries. "
                    f"{error_count} errors occurred."
                )
            )

            # Ask if user wants to backup the original file
            backup_choice = input("Create backup of original JSON file? (y/n): ")
            if backup_choice.lower() == "y":
                backup_filename = f"{json_file}.backup"
                os.rename(json_file, backup_filename)
                self.stdout.write(
                    self.style.SUCCESS(f"Original file backed up to {backup_filename}")
                )
            else:
                self.stdout.write(
                    self.style.WARNING("Original JSON file not backed up")
                )

        except json.JSONDecodeError as e:
            self.stdout.write(
                self.style.ERROR(f"Error parsing JSON file: {str(e)}")
            )
        except Exception as e:
            self.stdout.write(
                self.style.ERROR(f"Migration error: {str(e)}")
            )
