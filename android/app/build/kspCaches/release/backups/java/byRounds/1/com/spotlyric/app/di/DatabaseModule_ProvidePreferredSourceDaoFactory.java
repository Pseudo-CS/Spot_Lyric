package com.spotlyric.app.di;

import com.spotlyric.app.data.local.db.SpotLyricDatabase;
import com.spotlyric.app.data.local.db.dao.PreferredSourceDao;
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
public final class DatabaseModule_ProvidePreferredSourceDaoFactory implements Factory<PreferredSourceDao> {
  private final Provider<SpotLyricDatabase> dbProvider;

  public DatabaseModule_ProvidePreferredSourceDaoFactory(Provider<SpotLyricDatabase> dbProvider) {
    this.dbProvider = dbProvider;
  }

  @Override
  public PreferredSourceDao get() {
    return providePreferredSourceDao(dbProvider.get());
  }

  public static DatabaseModule_ProvidePreferredSourceDaoFactory create(
      Provider<SpotLyricDatabase> dbProvider) {
    return new DatabaseModule_ProvidePreferredSourceDaoFactory(dbProvider);
  }

  public static PreferredSourceDao providePreferredSourceDao(SpotLyricDatabase db) {
    return Preconditions.checkNotNullFromProvides(DatabaseModule.INSTANCE.providePreferredSourceDao(db));
  }
}
