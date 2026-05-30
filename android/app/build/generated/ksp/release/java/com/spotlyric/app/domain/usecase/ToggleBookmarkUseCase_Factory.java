package com.spotlyric.app.domain.usecase;

import com.spotlyric.app.domain.repository.PlayerRepository;
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
public final class ToggleBookmarkUseCase_Factory implements Factory<ToggleBookmarkUseCase> {
  private final Provider<PlayerRepository> playerRepositoryProvider;

  public ToggleBookmarkUseCase_Factory(Provider<PlayerRepository> playerRepositoryProvider) {
    this.playerRepositoryProvider = playerRepositoryProvider;
  }

  @Override
  public ToggleBookmarkUseCase get() {
    return newInstance(playerRepositoryProvider.get());
  }

  public static ToggleBookmarkUseCase_Factory create(
      Provider<PlayerRepository> playerRepositoryProvider) {
    return new ToggleBookmarkUseCase_Factory(playerRepositoryProvider);
  }

  public static ToggleBookmarkUseCase newInstance(PlayerRepository playerRepository) {
    return new ToggleBookmarkUseCase(playerRepository);
  }
}
