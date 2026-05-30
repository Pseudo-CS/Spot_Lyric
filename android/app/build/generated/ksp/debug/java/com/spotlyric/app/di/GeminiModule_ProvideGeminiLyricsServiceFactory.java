package com.spotlyric.app.di;

import com.spotlyric.app.data.local.datastore.SettingsPreferences;
import com.spotlyric.app.data.remote.gemini.GeminiLyricsService;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
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
public final class GeminiModule_ProvideGeminiLyricsServiceFactory implements Factory<GeminiLyricsService> {
  private final Provider<SettingsPreferences> settingsPreferencesProvider;

  public GeminiModule_ProvideGeminiLyricsServiceFactory(
      Provider<SettingsPreferences> settingsPreferencesProvider) {
    this.settingsPreferencesProvider = settingsPreferencesProvider;
  }

  @Override
  public GeminiLyricsService get() {
    return provideGeminiLyricsService(settingsPreferencesProvider.get());
  }

  public static GeminiModule_ProvideGeminiLyricsServiceFactory create(
      Provider<SettingsPreferences> settingsPreferencesProvider) {
    return new GeminiModule_ProvideGeminiLyricsServiceFactory(settingsPreferencesProvider);
  }

  public static GeminiLyricsService provideGeminiLyricsService(
      SettingsPreferences settingsPreferences) {
    return Preconditions.checkNotNullFromProvides(GeminiModule.INSTANCE.provideGeminiLyricsService(settingsPreferences));
  }
}
