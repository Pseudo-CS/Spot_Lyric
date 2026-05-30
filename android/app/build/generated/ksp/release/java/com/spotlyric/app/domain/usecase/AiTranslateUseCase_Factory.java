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
public final class AiTranslateUseCase_Factory implements Factory<AiTranslateUseCase> {
  private final Provider<PlayerRepository> playerRepositoryProvider;

  public AiTranslateUseCase_Factory(Provider<PlayerRepository> playerRepositoryProvider) {
    this.playerRepositoryProvider = playerRepositoryProvider;
  }

  @Override
  public AiTranslateUseCase get() {
    return newInstance(playerRepositoryProvider.get());
  }

  public static AiTranslateUseCase_Factory create(
      Provider<PlayerRepository> playerRepositoryProvider) {
    return new AiTranslateUseCase_Factory(playerRepositoryProvider);
  }

  public static AiTranslateUseCase newInstance(PlayerRepository playerRepository) {
    return new AiTranslateUseCase(playerRepository);
  }
}
