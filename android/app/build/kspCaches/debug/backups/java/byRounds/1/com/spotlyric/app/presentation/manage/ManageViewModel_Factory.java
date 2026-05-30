package com.spotlyric.app.presentation.manage;

import com.spotlyric.app.domain.repository.ManageRepository;
import com.spotlyric.app.domain.usecase.DeleteSongUseCase;
import com.spotlyric.app.domain.usecase.GetSongsUseCase;
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
public final class ManageViewModel_Factory implements Factory<ManageViewModel> {
  private final Provider<GetSongsUseCase> getSongsUseCaseProvider;

  private final Provider<DeleteSongUseCase> deleteSongUseCaseProvider;

  private final Provider<SearchLyricsSourcesUseCase> searchLyricsSourcesUseCaseProvider;

  private final Provider<ManageRepository> manageRepositoryProvider;

  public ManageViewModel_Factory(Provider<GetSongsUseCase> getSongsUseCaseProvider,
      Provider<DeleteSongUseCase> deleteSongUseCaseProvider,
      Provider<SearchLyricsSourcesUseCase> searchLyricsSourcesUseCaseProvider,
      Provider<ManageRepository> manageRepositoryProvider) {
    this.getSongsUseCaseProvider = getSongsUseCaseProvider;
    this.deleteSongUseCaseProvider = deleteSongUseCaseProvider;
    this.searchLyricsSourcesUseCaseProvider = searchLyricsSourcesUseCaseProvider;
    this.manageRepositoryProvider = manageRepositoryProvider;
  }

  @Override
  public ManageViewModel get() {
    return newInstance(getSongsUseCaseProvider.get(), deleteSongUseCaseProvider.get(), searchLyricsSourcesUseCaseProvider.get(), manageRepositoryProvider.get());
  }

  public static ManageViewModel_Factory create(Provider<GetSongsUseCase> getSongsUseCaseProvider,
      Provider<DeleteSongUseCase> deleteSongUseCaseProvider,
      Provider<SearchLyricsSourcesUseCase> searchLyricsSourcesUseCaseProvider,
      Provider<ManageRepository> manageRepositoryProvider) {
    return new ManageViewModel_Factory(getSongsUseCaseProvider, deleteSongUseCaseProvider, searchLyricsSourcesUseCaseProvider, manageRepositoryProvider);
  }

  public static ManageViewModel newInstance(GetSongsUseCase getSongsUseCase,
      DeleteSongUseCase deleteSongUseCase, SearchLyricsSourcesUseCase searchLyricsSourcesUseCase,
      ManageRepository manageRepository) {
    return new ManageViewModel(getSongsUseCase, deleteSongUseCase, searchLyricsSourcesUseCase, manageRepository);
  }
}
