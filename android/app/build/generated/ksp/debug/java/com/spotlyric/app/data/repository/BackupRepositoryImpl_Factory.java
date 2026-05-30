package com.spotlyric.app.data.repository;

import com.spotlyric.app.data.local.datastore.SettingsPreferences;
import com.spotlyric.app.data.local.db.dao.BookmarkedSongDao;
import com.spotlyric.app.data.local.db.dao.PreferredSourceDao;
import com.spotlyric.app.data.local.db.dao.SongLyricsDao;
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
public final class BackupRepositoryImpl_Factory implements Factory<BackupRepositoryImpl> {
  private final Provider<BookmarkedSongDao> bookmarkedSongDaoProvider;

  private final Provider<SongLyricsDao> songLyricsDaoProvider;

  private final Provider<PreferredSourceDao> preferredSourceDaoProvider;

  private final Provider<SettingsPreferences> settingsPreferencesProvider;

  public BackupRepositoryImpl_Factory(Provider<BookmarkedSongDao> bookmarkedSongDaoProvider,
      Provider<SongLyricsDao> songLyricsDaoProvider,
      Provider<PreferredSourceDao> preferredSourceDaoProvider,
      Provider<SettingsPreferences> settingsPreferencesProvider) {
    this.bookmarkedSongDaoProvider = bookmarkedSongDaoProvider;
    this.songLyricsDaoProvider = songLyricsDaoProvider;
    this.preferredSourceDaoProvider = preferredSourceDaoProvider;
    this.settingsPreferencesProvider = settingsPreferencesProvider;
  }

  @Override
  public BackupRepositoryImpl get() {
    return newInstance(bookmarkedSongDaoProvider.get(), songLyricsDaoProvider.get(), preferredSourceDaoProvider.get(), settingsPreferencesProvider.get());
  }

  public static BackupRepositoryImpl_Factory create(
      Provider<BookmarkedSongDao> bookmarkedSongDaoProvider,
      Provider<SongLyricsDao> songLyricsDaoProvider,
      Provider<PreferredSourceDao> preferredSourceDaoProvider,
      Provider<SettingsPreferences> settingsPreferencesProvider) {
    return new BackupRepositoryImpl_Factory(bookmarkedSongDaoProvider, songLyricsDaoProvider, preferredSourceDaoProvider, settingsPreferencesProvider);
  }

  public static BackupRepositoryImpl newInstance(BookmarkedSongDao bookmarkedSongDao,
      SongLyricsDao songLyricsDao, PreferredSourceDao preferredSourceDao,
      SettingsPreferences settingsPreferences) {
    return new BackupRepositoryImpl(bookmarkedSongDao, songLyricsDao, preferredSourceDao, settingsPreferences);
  }
}
