package com.spotlyric.app.di;

import com.spotlyric.app.data.local.db.SpotLyricDatabase;
import com.spotlyric.app.data.local.db.dao.BookmarkedSongDao;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata
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
public final class DatabaseModule_ProvideBookmarkedSongDaoFactory implements Factory<BookmarkedSongDao> {
  private final Provider<SpotLyricDatabase> dbProvider;

  public DatabaseModule_ProvideBookmarkedSongDaoFactory(Provider<SpotLyricDatabase> dbProvider) {
    this.dbProvider = dbProvider;
  }

  @Override
  public BookmarkedSongDao get() {
    return provideBookmarkedSongDao(dbProvider.get());
  }

  public static DatabaseModule_ProvideBookmarkedSongDaoFactory create(
      Provider<SpotLyricDatabase> dbProvider) {
    return new DatabaseModule_ProvideBookmarkedSongDaoFactory(dbProvider);
  }

  public static BookmarkedSongDao provideBookmarkedSongDao(SpotLyricDatabase db) {
    return Preconditions.checkNotNullFromProvides(DatabaseModule.INSTANCE.provideBookmarkedSongDao(db));
  }
}
