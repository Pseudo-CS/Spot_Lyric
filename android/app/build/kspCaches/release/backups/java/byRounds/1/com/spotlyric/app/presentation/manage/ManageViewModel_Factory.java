package com.spotlyric.app.presentation.manage;

import android.content.Context;
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
@QualifierMetadata("dagger.hilt.android.qualifiers.ApplicationContext")
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
  private final Provider<Context> contextProvider;

  private final Provider<GetSongsUseCase> getSongsUseCaseProvider;

  private final Provider<DeleteSongUseCase> deleteSongUseCaseProvider;

  private final Provider<SearchLyricsSourcesUseCase> searchLyricsSourcesUseCaseProvider;

  private final Provider<ManageRepository> manageRepositoryProvider;

  public ManageViewModel_Factory(Provider<Context> contextProvider,
      Provider<GetSongsUseCase> getSongsUseCaseProvider,
      Provider<DeleteSongUseCase> deleteSongUseCaseProvider,
      Provider<SearchLyricsSourcesUseCase> searchLyricsSourcesUseCaseProvider,
      Provider<ManageRepository> manageRepositoryProvider) {
    this.contextProvider = contextProvider;
    this.getSongsUseCaseProvider = getSongsUseCaseProvider;
    this.deleteSongUseCaseProvider = deleteSongUseCaseProvider;
    this.searchLyricsSourcesUseCaseProvider = searchLyricsSourcesUseCaseProvider;
    this.manageRepositoryProvider = manageRepositoryProvider;
  }

  @Override
  public ManageViewModel get() {
    return newInstance(contextProvider.get(), getSongsUseCaseProvider.get(), deleteSongUseCaseProvider.get(), searchLyricsSourcesUseCaseProvider.get(), manageRepositoryProvider.get());
  }

  public static ManageViewModel_Factory create(Provider<Context> contextProvider,
      Provider<GetSongsUseCase> getSongsUseCaseProvider,
      Provider<DeleteSongUseCase> deleteSongUseCaseProvider,
      Provider<SearchLyricsSourcesUseCase> searchLyricsSourcesUseCaseProvider,
      Provider<ManageRepository> manageRepositoryProvider) {
    return new ManageViewModel_Factory(contextProvider, getSongsUseCaseProvider, deleteSongUseCaseProvider, searchLyricsSourcesUseCaseProvider, manageRepositoryProvider);
  }

  public static ManageViewModel newInstance(Context context, GetSongsUseCase getSongsUseCase,
      DeleteSongUseCase deleteSongUseCase, SearchLyricsSourcesUseCase searchLyricsSourcesUseCase,
      ManageRepository manageRepository) {
    return new ManageViewModel(context, getSongsUseCase, deleteSongUseCase, searchLyricsSourcesUseCase, manageRepository);
  }
}
