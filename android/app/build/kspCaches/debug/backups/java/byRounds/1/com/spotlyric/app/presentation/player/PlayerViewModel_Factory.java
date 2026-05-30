package com.spotlyric.app.presentation.player;

import com.spotlyric.app.domain.repository.PlayerRepository;
import com.spotlyric.app.domain.usecase.ExtractAndTranslateUseCase;
import com.spotlyric.app.domain.usecase.GetCurrentSongUseCase;
import com.spotlyric.app.domain.usecase.GetLyricsUseCase;
import com.spotlyric.app.domain.usecase.SearchLyricsSourcesUseCase;
import com.spotlyric.app.domain.usecase.ToggleBookmarkUseCase;
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
public final class PlayerViewModel_Factory implements Factory<PlayerViewModel> {
  private final Provider<GetCurrentSongUseCase> getCurrentSongUseCaseProvider;

  private final Provider<SearchLyricsSourcesUseCase> searchLyricsSourcesUseCaseProvider;

  private final Provider<ToggleBookmarkUseCase> toggleBookmarkUseCaseProvider;

  private final Provider<ExtractAndTranslateUseCase> extractAndTranslateUseCaseProvider;

  private final Provider<GetLyricsUseCase> getLyricsUseCaseProvider;

  private final Provider<PlayerRepository> playerRepositoryProvider;

  public PlayerViewModel_Factory(Provider<GetCurrentSongUseCase> getCurrentSongUseCaseProvider,
      Provider<SearchLyricsSourcesUseCase> searchLyricsSourcesUseCaseProvider,
      Provider<ToggleBookmarkUseCase> toggleBookmarkUseCaseProvider,
      Provider<ExtractAndTranslateUseCase> extractAndTranslateUseCaseProvider,
      Provider<GetLyricsUseCase> getLyricsUseCaseProvider,
      Provider<PlayerRepository> playerRepositoryProvider) {
    this.getCurrentSongUseCaseProvider = getCurrentSongUseCaseProvider;
    this.searchLyricsSourcesUseCaseProvider = searchLyricsSourcesUseCaseProvider;
    this.toggleBookmarkUseCaseProvider = toggleBookmarkUseCaseProvider;
    this.extractAndTranslateUseCaseProvider = extractAndTranslateUseCaseProvider;
    this.getLyricsUseCaseProvider = getLyricsUseCaseProvider;
    this.playerRepositoryProvider = playerRepositoryProvider;
  }

  @Override
  public PlayerViewModel get() {
    return newInstance(getCurrentSongUseCaseProvider.get(), searchLyricsSourcesUseCaseProvider.get(), toggleBookmarkUseCaseProvider.get(), extractAndTranslateUseCaseProvider.get(), getLyricsUseCaseProvider.get(), playerRepositoryProvider.get());
  }

  public static PlayerViewModel_Factory create(
      Provider<GetCurrentSongUseCase> getCurrentSongUseCaseProvider,
      Provider<SearchLyricsSourcesUseCase> searchLyricsSourcesUseCaseProvider,
      Provider<ToggleBookmarkUseCase> toggleBookmarkUseCaseProvider,
      Provider<ExtractAndTranslateUseCase> extractAndTranslateUseCaseProvider,
      Provider<GetLyricsUseCase> getLyricsUseCaseProvider,
      Provider<PlayerRepository> playerRepositoryProvider) {
    return new PlayerViewModel_Factory(getCurrentSongUseCaseProvider, searchLyricsSourcesUseCaseProvider, toggleBookmarkUseCaseProvider, extractAndTranslateUseCaseProvider, getLyricsUseCaseProvider, playerRepositoryProvider);
  }

  public static PlayerViewModel newInstance(GetCurrentSongUseCase getCurrentSongUseCase,
      SearchLyricsSourcesUseCase searchLyricsSourcesUseCase,
      ToggleBookmarkUseCase toggleBookmarkUseCase,
      ExtractAndTranslateUseCase extractAndTranslateUseCase, GetLyricsUseCase getLyricsUseCase,
      PlayerRepository playerRepository) {
    return new PlayerViewModel(getCurrentSongUseCase, searchLyricsSourcesUseCase, toggleBookmarkUseCase, extractAndTranslateUseCase, getLyricsUseCase, playerRepository);
  }
}
