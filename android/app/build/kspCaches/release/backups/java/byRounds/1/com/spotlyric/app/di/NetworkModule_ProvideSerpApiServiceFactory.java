package com.spotlyric.app.di;

import com.spotlyric.app.data.remote.serpapi.SerpApiService;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;
import retrofit2.Retrofit;

@ScopeMetadata("javax.inject.Singleton")
@QualifierMetadata("javax.inject.Named")
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
public final class NetworkModule_ProvideSerpApiServiceFactory implements Factory<SerpApiService> {
  private final Provider<Retrofit> retrofitProvider;

  public NetworkModule_ProvideSerpApiServiceFactory(Provider<Retrofit> retrofitProvider) {
    this.retrofitProvider = retrofitProvider;
  }

  @Override
  public SerpApiService get() {
    return provideSerpApiService(retrofitProvider.get());
  }

  public static NetworkModule_ProvideSerpApiServiceFactory create(
      Provider<Retrofit> retrofitProvider) {
    return new NetworkModule_ProvideSerpApiServiceFactory(retrofitProvider);
  }

  public static SerpApiService provideSerpApiService(Retrofit retrofit) {
    return Preconditions.checkNotNullFromProvides(NetworkModule.INSTANCE.provideSerpApiService(retrofit));
  }
}
