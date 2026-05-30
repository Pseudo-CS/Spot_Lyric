package com.spotlyric.app.domain.usecase;

import com.spotlyric.app.domain.repository.PlayerRepository;
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
public final class SearchLyricsSourcesUseCase_Factory implements Factory<SearchLyricsSourcesUseCase> {
  private final Provider<PlayerRepository> playerRepositoryProvider;

  private final Provider<SourcesRepository> sourcesRepositoryProvider;

  public SearchLyricsSourcesUseCase_Factory(Provider<PlayerRepository> playerRepositoryProvider,
      Provider<SourcesRepository> sourcesRepositoryProvider) {
    this.playerRepositoryProvider = playerRepositoryProvider;
    this.sourcesRepositoryProvider = sourcesRepositoryProvider;
  }

  @Override
  public SearchLyricsSourcesUseCase get() {
    return newInstance(playerRepositoryProvider.get(), sourcesRepositoryProvider.get());
  }

  public static SearchLyricsSourcesUseCase_Factory create(
      Provider<PlayerRepository> playerRepositoryProvider,
      Provider<SourcesRepository> sourcesRepositoryProvider) {
    return new SearchLyricsSourcesUseCase_Factory(playerRepositoryProvider, sourcesRepositoryProvider);
  }

  public static SearchLyricsSourcesUseCase newInstance(PlayerRepository playerRepository,
      SourcesRepository sourcesRepository) {
    return new SearchLyricsSourcesUseCase(playerRepository, sourcesRepository);
  }
}
