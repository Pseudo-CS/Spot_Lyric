package com.spotlyric.app.data.remote.extractor;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
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
public final class LyricsExtractorService_Factory implements Factory<LyricsExtractorService> {
  private final Provider<OkHttpClient> okHttpClientProvider;

  public LyricsExtractorService_Factory(Provider<OkHttpClient> okHttpClientProvider) {
    this.okHttpClientProvider = okHttpClientProvider;
  }

  @Override
  public LyricsExtractorService get() {
    return newInstance(okHttpClientProvider.get());
  }

  public static LyricsExtractorService_Factory create(Provider<OkHttpClient> okHttpClientProvider) {
    return new LyricsExtractorService_Factory(okHttpClientProvider);
  }

  public static LyricsExtractorService newInstance(OkHttpClient okHttpClient) {
    return new LyricsExtractorService(okHttpClient);
  }
}
