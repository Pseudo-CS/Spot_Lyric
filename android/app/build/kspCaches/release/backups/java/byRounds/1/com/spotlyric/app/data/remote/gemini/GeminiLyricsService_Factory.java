package com.spotlyric.app.data.remote.gemini;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

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
public final class GeminiLyricsService_Factory implements Factory<GeminiLyricsService> {
  private final Provider<String> apiKeyProvider;

  public GeminiLyricsService_Factory(Provider<String> apiKeyProvider) {
    this.apiKeyProvider = apiKeyProvider;
  }

  @Override
  public GeminiLyricsService get() {
    return newInstance(apiKeyProvider.get());
  }

  public static GeminiLyricsService_Factory create(Provider<String> apiKeyProvider) {
    return new GeminiLyricsService_Factory(apiKeyProvider);
  }

  public static GeminiLyricsService newInstance(String apiKey) {
    return new GeminiLyricsService(apiKey);
  }
}
