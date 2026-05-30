package com.spotlyric.app.domain.usecase;

import com.spotlyric.app.domain.repository.AuthRepository;
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
public final class GetCurrentSongUseCase_Factory implements Factory<GetCurrentSongUseCase> {
  private final Provider<AuthRepository> authRepositoryProvider;

  private final Provider<PlayerRepository> playerRepositoryProvider;

  public GetCurrentSongUseCase_Factory(Provider<AuthRepository> authRepositoryProvider,
      Provider<PlayerRepository> playerRepositoryProvider) {
    this.authRepositoryProvider = authRepositoryProvider;
    this.playerRepositoryProvider = playerRepositoryProvider;
  }

  @Override
  public GetCurrentSongUseCase get() {
    return newInstance(authRepositoryProvider.get(), playerRepositoryProvider.get());
  }

  public static GetCurrentSongUseCase_Factory create(
      Provider<AuthRepository> authRepositoryProvider,
      Provider<PlayerRepository> playerRepositoryProvider) {
    return new GetCurrentSongUseCase_Factory(authRepositoryProvider, playerRepositoryProvider);
  }

  public static GetCurrentSongUseCase newInstance(AuthRepository authRepository,
      PlayerRepository playerRepository) {
    return new GetCurrentSongUseCase(authRepository, playerRepository);
  }
}
