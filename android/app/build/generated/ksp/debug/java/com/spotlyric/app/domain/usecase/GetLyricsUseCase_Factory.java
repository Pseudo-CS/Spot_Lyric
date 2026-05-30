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
public final class GetLyricsUseCase_Factory implements Factory<GetLyricsUseCase> {
  private final Provider<PlayerRepository> playerRepositoryProvider;

  public GetLyricsUseCase_Factory(Provider<PlayerRepository> playerRepositoryProvider) {
    this.playerRepositoryProvider = playerRepositoryProvider;
  }

  @Override
  public GetLyricsUseCase get() {
    return newInstance(playerRepositoryProvider.get());
  }

  public static GetLyricsUseCase_Factory create(
      Provider<PlayerRepository> playerRepositoryProvider) {
    return new GetLyricsUseCase_Factory(playerRepositoryProvider);
  }

  public static GetLyricsUseCase newInstance(PlayerRepository playerRepository) {
    return new GetLyricsUseCase(playerRepository);
  }
}
