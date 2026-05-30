from django.urls import path
from . import views

app_name = "spotify"

urlpatterns = [
    path("", views.spotify_view, name="index"),
    path("login/", views.spotify_login, name="login"),
    path("callback", views.spotify_callback, name="callback"),
    path("toggle-bookmark/", views.spotify_toggle_bookmark, name="toggle-bookmark"),
    path("current-song/", views.spotify_current_song, name="current-song"),
    path("lyrics/", views.spotify_lyrics_page, name="lyrics-page"),
    path("extract-lyrics/", views.spotify_extract_lyrics, name="extract-lyrics"),
    path("ai-translate/", views.spotify_ai_translate, name="ai-translate"),
    path("ai-translate-search/", views.spotify_ai_translate_search, name="ai-translate-search"),
    path("extract-and-translate/", views.spotify_extract_and_translate, name="extract-and-translate"),
    path("test-gemini/", views.spotify_test_gemini, name="test-gemini"),
    path("manage/", views.spotify_manage_songs, name="manage-songs"),
    path("delete-song/", views.spotify_delete_song, name="delete-song"),
    path("server-logs/", views.spotify_server_logs, name="server-logs"),
]
