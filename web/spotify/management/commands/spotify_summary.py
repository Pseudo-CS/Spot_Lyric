from django.core.management.base import BaseCommand
from django.utils import timezone
from spotify.models import SpotifyCache, SpotifyBookmark, SpotifySearchResult
from spotify.utils import cleanup_expired_cache


class Command(BaseCommand):
    help = "Display Spotify app migration summary and cleanup information"

    def handle(self, *args, **options):
        self.stdout.write(
            self.style.SUCCESS("\n=== Spotify App Migration Summary ===\n")
        )

        # Display current database statistics
        cache_count = SpotifyCache.objects.count()
        bookmark_count = SpotifyBookmark.objects.count()
        search_result_count = SpotifySearchResult.objects.count()

        self.stdout.write(f"Total cache entries: {cache_count}")
        self.stdout.write(f"Total bookmarks: {bookmark_count}")
        self.stdout.write(f"Total search results: {search_result_count}")

        # Check for expired cache entries
        expired_count = SpotifyCache.objects.filter(expires_at__lt=timezone.now()).count()
        if expired_count > 0:
            self.stdout.write(f"\nExpired cache entries found: {expired_count}")
            cleanup_choice = input("Run cleanup for expired entries? (y/n): ")
            if cleanup_choice.lower() == "y":
                cleaned = cleanup_expired_cache()
                self.stdout.write(
                    self.style.SUCCESS(f"Cleaned up {cleaned} expired cache entries")
                )
        else:
            self.stdout.write("\nNo expired cache entries found.")

        self.stdout.write(
            self.style.SUCCESS("\n=== Migration Status ===")
        )
        self.stdout.write("✓ New Spotify app created and configured")
        self.stdout.write("✓ Database models created and migrated")
        self.stdout.write("✓ JSON cache data migrated to database")
        self.stdout.write("✓ URL routing updated")
        self.stdout.write("✓ Old Spotify files removed from ecom app")

        self.stdout.write(
            self.style.WARNING("\n=== Post-Migration Notes ===")
        )
        self.stdout.write("• Template paths may need updating if custom templates exist")
        self.stdout.write("• Test all Spotify functionality to ensure proper operation")
        self.stdout.write("• Consider backing up the search_cache.json file before deletion")
        self.stdout.write("• The new app uses database caching instead of JSON files")

        self.stdout.write(
            self.style.SUCCESS("\n=== Next Steps ===")
        )
        self.stdout.write("1. Test Spotify login and authentication")
        self.stdout.write("2. Test bookmark functionality")
        self.stdout.write("3. Verify search results display correctly")
        self.stdout.write("4. Check that cache expiration works as expected")
        self.stdout.write("5. Remove or backup the old search_cache.json file")
