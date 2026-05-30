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
public final class ExtractAndTranslateUseCase_Factory implements Factory<ExtractAndTranslateUseCase> {
  private final Provider<PlayerRepository> playerRepositoryProvider;

  public ExtractAndTranslateUseCase_Factory(Provider<PlayerRepository> playerRepositoryProvider) {
    this.playerRepositoryProvider = playerRepositoryProvider;
  }

  @Override
  public ExtractAndTranslateUseCase get() {
    return newInstance(playerRepositoryProvider.get());
  }

  public static ExtractAndTranslateUseCase_Factory create(
      Provider<PlayerRepository> playerRepositoryProvider) {
    return new ExtractAndTranslateUseCase_Factory(playerRepositoryProvider);
  }

  public static ExtractAndTranslateUseCase newInstance(PlayerRepository playerRepository) {
    return new ExtractAndTranslateUseCase(playerRepository);
  }
}
