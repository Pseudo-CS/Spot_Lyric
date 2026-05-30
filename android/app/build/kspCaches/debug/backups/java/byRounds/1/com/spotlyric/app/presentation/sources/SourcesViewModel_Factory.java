package com.spotlyric.app.presentation.sources;

import com.spotlyric.app.domain.repository.SourcesRepository;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
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
public final class SourcesViewModel_Factory implements Factory<SourcesViewModel> {
  private final Provider<SourcesRepository> sourcesRepositoryProvider;

  public SourcesViewModel_Factory(Provider<SourcesRepository> sourcesRepositoryProvider) {
    this.sourcesRepositoryProvider = sourcesRepositoryProvider;
  }

  @Override
  public SourcesViewModel get() {
    return newInstance(sourcesRepositoryProvider.get());
  }

  public static SourcesViewModel_Factory create(
      Provider<SourcesRepository> sourcesRepositoryProvider) {
    return new SourcesViewModel_Factory(sourcesRepositoryProvider);
  }

  public static SourcesViewModel newInstance(SourcesRepository sourcesRepository) {
    return new SourcesViewModel(sourcesRepository);
  }
}
