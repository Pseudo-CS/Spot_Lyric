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
public final class GetSongsUseCase_Factory implements Factory<GetSongsUseCase> {
  private final Provider<ManageRepository> manageRepositoryProvider;

  public GetSongsUseCase_Factory(Provider<ManageRepository> manageRepositoryProvider) {
    this.manageRepositoryProvider = manageRepositoryProvider;
  }

  @Override
  public GetSongsUseCase get() {
    return newInstance(manageRepositoryProvider.get());
  }

  public static GetSongsUseCase_Factory create(
      Provider<ManageRepository> manageRepositoryProvider) {
    return new GetSongsUseCase_Factory(manageRepositoryProvider);
  }

  public static GetSongsUseCase newInstance(ManageRepository manageRepository) {
    return new GetSongsUseCase(manageRepository);
  }
}
