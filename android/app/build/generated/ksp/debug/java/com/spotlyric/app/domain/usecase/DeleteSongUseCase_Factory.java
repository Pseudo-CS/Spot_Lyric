package com.spotlyric.app.domain.usecase;

import com.spotlyric.app.domain.repository.ManageRepository;
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
public final class DeleteSongUseCase_Factory implements Factory<DeleteSongUseCase> {
  private final Provider<ManageRepository> manageRepositoryProvider;

  public DeleteSongUseCase_Factory(Provider<ManageRepository> manageRepositoryProvider) {
    this.manageRepositoryProvider = manageRepositoryProvider;
  }

  @Override
  public DeleteSongUseCase get() {
    return newInstance(manageRepositoryProvider.get());
  }

  public static DeleteSongUseCase_Factory create(
      Provider<ManageRepository> manageRepositoryProvider) {
    return new DeleteSongUseCase_Factory(manageRepositoryProvider);
  }

  public static DeleteSongUseCase newInstance(ManageRepository manageRepository) {
    return new DeleteSongUseCase(manageRepository);
  }
}
