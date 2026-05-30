package com.spotlyric.app.data.repository;

import com.spotlyric.app.data.local.db.dao.PreferredSourceDao;
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
public final class SourcesRepositoryImpl_Factory implements Factory<SourcesRepositoryImpl> {
  private final Provider<PreferredSourceDao> daoProvider;

  public SourcesRepositoryImpl_Factory(Provider<PreferredSourceDao> daoProvider) {
    this.daoProvider = daoProvider;
  }

  @Override
  public SourcesRepositoryImpl get() {
    return newInstance(daoProvider.get());
  }

  public static SourcesRepositoryImpl_Factory create(Provider<PreferredSourceDao> daoProvider) {
    return new SourcesRepositoryImpl_Factory(daoProvider);
  }

  public static SourcesRepositoryImpl newInstance(PreferredSourceDao dao) {
    return new SourcesRepositoryImpl(dao);
  }
}
