package com.spotlyric.app.di;

import com.spotlyric.app.data.remote.gemini.GeminiLyricsService;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;

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
public final class GeminiModule_ProvideGeminiLyricsServiceFactory implements Factory<GeminiLyricsService> {
  @Override
  public GeminiLyricsService get() {
    return provideGeminiLyricsService();
  }

  public static GeminiModule_ProvideGeminiLyricsServiceFactory create() {
    return InstanceHolder.INSTANCE;
  }

  public static GeminiLyricsService provideGeminiLyricsService() {
    return Preconditions.checkNotNullFromProvides(GeminiModule.INSTANCE.provideGeminiLyricsService());
  }

  private static final class InstanceHolder {
    private static final GeminiModule_ProvideGeminiLyricsServiceFactory INSTANCE = new GeminiModule_ProvideGeminiLyricsServiceFactory();
  }
}
