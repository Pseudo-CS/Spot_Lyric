package com.spotlyric.app.data.repository;

import com.spotlyric.app.data.local.datastore.SettingsPreferences;
import com.spotlyric.app.data.local.db.dao.BookmarkedSongDao;
import com.spotlyric.app.data.local.db.dao.SongLyricsDao;
import com.spotlyric.app.data.remote.extractor.LyricsExtractorService;
import com.spotlyric.app.data.remote.gemini.GeminiLyricsService;
import com.spotlyric.app.data.remote.serpapi.LyricsSearchService;
import com.spotlyric.app.data.remote.spotify.SpotifyApiService;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata("javax.inject.Singleton")
@QualifierMetadata
@DaggerGenerated
@Generated(
    value = "dagger.internal.codegen.ComponentProcessor",
    comments = "https://dagger.dev"
)
@SuppressWarnings({
    "unchecked",
    "rawtypes",
    "KotlinInternal",
    "KotlinInternalInJava",
    "cast",
    "deprecation"
})
public final class PlayerRepositoryImpl_Factory implements Factory<PlayerRepositoryImpl> {
  private final Provider<SpotifyApiService> spotifyApiServiceProvider;

  private final Provider<LyricsSearchService> lyricsSearchServiceProvider;

  private final Provider<LyricsExtractorService> lyricsExtractorServiceProvider;

  private final Provider<GeminiLyricsService> geminiLyricsServiceProvider;

  private final Provider<BookmarkedSongDao> bookmarkedSongDaoProvider;

  private final Provider<SongLyricsDao> songLyricsDaoProvider;

  private final Provider<SettingsPreferences> settingsPreferencesProvider;

  public PlayerRepositoryImpl_Factory(Provider<SpotifyApiService> spotifyApiServiceProvider,
      Provider<LyricsSearchService> lyricsSearchServiceProvider,
      Provider<LyricsExtractorService> lyricsExtractorServiceProvider,
      Provider<GeminiLyricsService> geminiLyricsServiceProvider,
      Provider<BookmarkedSongDao> bookmarkedSongDaoProvider,
      Provider<SongLyricsDao> songLyricsDaoProvider,
      Provider<SettingsPreferences> settingsPreferencesProvider) {
    this.spotifyApiServiceProvider = spotifyApiServiceProvider;
    this.lyricsSearchServiceProvider = lyricsSearchServiceProvider;
    this.lyricsExtractorServiceProvider = lyricsExtractorServiceProvider;
    this.geminiLyricsServiceProvider = geminiLyricsServiceProvider;
    this.bookmarkedSongDaoProvider = bookmarkedSongDaoProvider;
    this.songLyricsDaoProvider = songLyricsDaoProvider;
    this.settingsPreferencesProvider = settingsPreferencesProvider;
  }

  @Override
  public PlayerRepositoryImpl get() {
    return newInstance(spotifyApiServiceProvider.get(), lyricsSearchServiceProvider.get(), lyricsExtractorServiceProvider.get(), geminiLyricsServiceProvider.get(), bookmarkedSongDaoProvider.get(), songLyricsDaoProvider.get(), settingsPreferencesProvider.get());
  }

  public static PlayerRepositoryImpl_Factory create(
      Provider<SpotifyApiService> spotifyApiServiceProvider,
      Provider<LyricsSearchService> lyricsSearchServiceProvider,
      Provider<LyricsExtractorService> lyricsExtractorServiceProvider,
      Provider<GeminiLyricsService> geminiLyricsServiceProvider,
      Provider<BookmarkedSongDao> bookmarkedSongDaoProvider,
      Provider<SongLyricsDao> songLyricsDaoProvider,
      Provider<SettingsPreferences> settingsPreferencesProvider) {
    return new PlayerRepositoryImpl_Factory(spotifyApiServiceProvider, lyricsSearchServiceProvider, lyricsExtractorServiceProvider, geminiLyricsServiceProvider, bookmarkedSongDaoProvider, songLyricsDaoProvider, settingsPreferencesProvider);
  }

  public static PlayerRepositoryImpl newInstance(SpotifyApiService spotifyApiService,
      LyricsSearchService lyricsSearchService, LyricsExtractorService lyricsExtractorService,
      GeminiLyricsService geminiLyricsService, BookmarkedSongDao bookmarkedSongDao,
      SongLyricsDao songLyricsDao, SettingsPreferences settingsPreferences) {
    return new PlayerRepositoryImpl(spotifyApiService, lyricsSearchService, lyricsExtractorService, geminiLyricsService, bookmarkedSongDao, songLyricsDao, settingsPreferences);
  }
}
