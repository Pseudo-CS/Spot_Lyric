package com.spotlyric.app.data.repository;

import com.spotlyric.app.data.local.db.dao.BookmarkedSongDao;
import com.spotlyric.app.data.local.db.dao.SongLyricsDao;
import com.spotlyric.app.data.remote.extractor.LyricsExtractorService;
import com.spotlyric.app.data.remote.gemini.GeminiLyricsService;
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
public final class ManageRepositoryImpl_Factory implements Factory<ManageRepositoryImpl> {
  private final Provider<BookmarkedSongDao> bookmarkedSongDaoProvider;

  private final Provider<SongLyricsDao> songLyricsDaoProvider;

  private final Provider<LyricsExtractorService> lyricsExtractorServiceProvider;

  private final Provider<GeminiLyricsService> geminiLyricsServiceProvider;

  public ManageRepositoryImpl_Factory(Provider<BookmarkedSongDao> bookmarkedSongDaoProvider,
      Provider<SongLyricsDao> songLyricsDaoProvider,
      Provider<LyricsExtractorService> lyricsExtractorServiceProvider,
      Provider<GeminiLyricsService> geminiLyricsServiceProvider) {
    this.bookmarkedSongDaoProvider = bookmarkedSongDaoProvider;
    this.songLyricsDaoProvider = songLyricsDaoProvider;
    this.lyricsExtractorServiceProvider = lyricsExtractorServiceProvider;
    this.geminiLyricsServiceProvider = geminiLyricsServiceProvider;
  }

  @Override
  public ManageRepositoryImpl get() {
    return newInstance(bookmarkedSongDaoProvider.get(), songLyricsDaoProvider.get(), lyricsExtractorServiceProvider.get(), geminiLyricsServiceProvider.get());
  }

  public static ManageRepositoryImpl_Factory create(
      Provider<BookmarkedSongDao> bookmarkedSongDaoProvider,
      Provider<SongLyricsDao> songLyricsDaoProvider,
      Provider<LyricsExtractorService> lyricsExtractorServiceProvider,
      Provider<GeminiLyricsService> geminiLyricsServiceProvider) {
    return new ManageRepositoryImpl_Factory(bookmarkedSongDaoProvider, songLyricsDaoProvider, lyricsExtractorServiceProvider, geminiLyricsServiceProvider);
  }

  public static ManageRepositoryImpl newInstance(BookmarkedSongDao bookmarkedSongDao,
      SongLyricsDao songLyricsDao, LyricsExtractorService lyricsExtractorService,
      GeminiLyricsService geminiLyricsService) {
    return new ManageRepositoryImpl(bookmarkedSongDao, songLyricsDao, lyricsExtractorService, geminiLyricsService);
  }
}
