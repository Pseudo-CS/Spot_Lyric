package com.spotlyric.app.presentation.lyrics;

import androidx.lifecycle.SavedStateHandle;
import com.spotlyric.app.domain.usecase.AiTranslateUseCase;
import com.spotlyric.app.domain.usecase.ExtractAndTranslateUseCase;
import com.spotlyric.app.domain.usecase.GetCurrentSongUseCase;
import com.spotlyric.app.domain.usecase.GetLyricsUseCase;
import com.spotlyric.app.domain.usecase.SearchLyricsSourcesUseCase;
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
public final class LyricsViewModel_Factory implements Factory<LyricsViewModel> {
  private final Provider<SavedStateHandle> savedStateHandleProvider;

  private final Provider<GetLyricsUseCase> getLyricsUseCaseProvider;

  private final Provider<AiTranslateUseCase> aiTranslateUseCaseProvider;

  private final Provider<SearchLyricsSourcesUseCase> searchLyricsSourcesUseCaseProvider;

  private final Provider<ExtractAndTranslateUseCase> extractAndTranslateUseCaseProvider;

  private final Provider<GetCurrentSongUseCase> getCurrentSongUseCaseProvider;

  public LyricsViewModel_Factory(Provider<SavedStateHandle> savedStateHandleProvider,
      Provider<GetLyricsUseCase> getLyricsUseCaseProvider,
      Provider<AiTranslateUseCase> aiTranslateUseCaseProvider,
      Provider<SearchLyricsSourcesUseCase> searchLyricsSourcesUseCaseProvider,
      Provider<ExtractAndTranslateUseCase> extractAndTranslateUseCaseProvider,
      Provider<GetCurrentSongUseCase> getCurrentSongUseCaseProvider) {
    this.savedStateHandleProvider = savedStateHandleProvider;
    this.getLyricsUseCaseProvider = getLyricsUseCaseProvider;
    this.aiTranslateUseCaseProvider = aiTranslateUseCaseProvider;
    this.searchLyricsSourcesUseCaseProvider = searchLyricsSourcesUseCaseProvider;
    this.extractAndTranslateUseCaseProvider = extractAndTranslateUseCaseProvider;
    this.getCurrentSongUseCaseProvider = getCurrentSongUseCaseProvider;
  }

  @Override
  public LyricsViewModel get() {
    return newInstance(savedStateHandleProvider.get(), getLyricsUseCaseProvider.get(), aiTranslateUseCaseProvider.get(), searchLyricsSourcesUseCaseProvider.get(), extractAndTranslateUseCaseProvider.get(), getCurrentSongUseCaseProvider.get());
  }

  public static LyricsViewModel_Factory create(Provider<SavedStateHandle> savedStateHandleProvider,
      Provider<GetLyricsUseCase> getLyricsUseCaseProvider,
      Provider<AiTranslateUseCase> aiTranslateUseCaseProvider,
      Provider<SearchLyricsSourcesUseCase> searchLyricsSourcesUseCaseProvider,
      Provider<ExtractAndTranslateUseCase> extractAndTranslateUseCaseProvider,
      Provider<GetCurrentSongUseCase> getCurrentSongUseCaseProvider) {
    return new LyricsViewModel_Factory(savedStateHandleProvider, getLyricsUseCaseProvider, aiTranslateUseCaseProvider, searchLyricsSourcesUseCaseProvider, extractAndTranslateUseCaseProvider, getCurrentSongUseCaseProvider);
  }

  public static LyricsViewModel newInstance(SavedStateHandle savedStateHandle,
      GetLyricsUseCase getLyricsUseCase, AiTranslateUseCase aiTranslateUseCase,
      SearchLyricsSourcesUseCase searchLyricsSourcesUseCase,
      ExtractAndTranslateUseCase extractAndTranslateUseCase,
      GetCurrentSongUseCase getCurrentSongUseCase) {
    return new LyricsViewModel(savedStateHandle, getLyricsUseCase, aiTranslateUseCase, searchLyricsSourcesUseCase, extractAndTranslateUseCase, getCurrentSongUseCase);
  }
}
