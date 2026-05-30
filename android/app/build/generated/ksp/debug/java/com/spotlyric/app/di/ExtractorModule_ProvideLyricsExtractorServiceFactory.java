package com.spotlyric.app.di;

import com.spotlyric.app.data.remote.extractor.LyricsExtractorService;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;
import okhttp3.OkHttpClient;

@ScopeMetadata("javax.inject.Singleton")
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
public final class ExtractorModule_ProvideLyricsExtractorServiceFactory implements Factory<LyricsExtractorService> {
  private final Provider<OkHttpClient> okHttpClientProvider;

  public ExtractorModule_ProvideLyricsExtractorServiceFactory(
      Provider<OkHttpClient> okHttpClientProvider) {
    this.okHttpClientProvider = okHttpClientProvider;
  }

  @Override
  public LyricsExtractorService get() {
    return provideLyricsExtractorService(okHttpClientProvider.get());
  }

  public static ExtractorModule_ProvideLyricsExtractorServiceFactory create(
      Provider<OkHttpClient> okHttpClientProvider) {
    return new ExtractorModule_ProvideLyricsExtractorServiceFactory(okHttpClientProvider);
  }

  public static LyricsExtractorService provideLyricsExtractorService(OkHttpClient okHttpClient) {
    return Preconditions.checkNotNullFromProvides(ExtractorModule.INSTANCE.provideLyricsExtractorService(okHttpClient));
  }
}
