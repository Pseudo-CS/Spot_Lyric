package com.spotlyric.app.di;

import com.spotlyric.app.data.local.db.SpotLyricDatabase;
import com.spotlyric.app.data.local.db.dao.SongLyricsDao;
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
public final class DatabaseModule_ProvideSongLyricsDaoFactory implements Factory<SongLyricsDao> {
  private final Provider<SpotLyricDatabase> dbProvider;

  public DatabaseModule_ProvideSongLyricsDaoFactory(Provider<SpotLyricDatabase> dbProvider) {
    this.dbProvider = dbProvider;
  }

  @Override
  public SongLyricsDao get() {
    return provideSongLyricsDao(dbProvider.get());
  }

  public static DatabaseModule_ProvideSongLyricsDaoFactory create(
      Provider<SpotLyricDatabase> dbProvider) {
    return new DatabaseModule_ProvideSongLyricsDaoFactory(dbProvider);
  }

  public static SongLyricsDao provideSongLyricsDao(SpotLyricDatabase db) {
    return Preconditions.checkNotNullFromProvides(DatabaseModule.INSTANCE.provideSongLyricsDao(db));
  }
}
