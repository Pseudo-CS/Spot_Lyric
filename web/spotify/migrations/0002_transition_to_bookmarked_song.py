# Custom migration to transition from old models to BookmarkedSong

from django.db import migrations, models


class Migration(migrations.Migration):

    dependencies = [
        ("spotify", "0001_initial"),
    ]

    operations = [
        migrations.CreateModel(
            name="BookmarkedSong",
            fields=[
                ("id", models.BigAutoField(auto_created=True, primary_key=True, serialize=False, verbose_name="ID")),
                ("song_name", models.CharField(max_length=255)),
                ("artist_name", models.CharField(blank=True, max_length=255, null=True)),
                ("bookmarked_url", models.URLField(max_length=1000)),
                ("title", models.CharField(blank=True, max_length=500, null=True)),
                ("created_at", models.DateTimeField(auto_now_add=True)),
            ],
            options={
                "db_table": "bookmarked_song",
                "indexes": [
                    models.Index(fields=["song_name"], name="bookmarked__song_na_72ee3c_idx"),
                    models.Index(fields=["song_name", "artist_name"], name="bookmarked__song_na_2cad53_idx"),
                ],
                "unique_together": {("song_name", "artist_name")},
            },
        ),
        migrations.AlterUniqueTogether(
            name="spotifycache",
            unique_together=set(),
        ),
        migrations.AlterUniqueTogether(
            name="spotifysearchresult",
            unique_together=set(),
        ),
        migrations.AlterUniqueTogether(
            name="spotifybookmark",
            unique_together=set(),
        ),
        migrations.DeleteModel(
            name="SpotifySearchResult",
        ),
        migrations.DeleteModel(
            name="SpotifyBookmark",
        ),
        migrations.DeleteModel(
            name="SpotifyCache",
        ),
    ]
