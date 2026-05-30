package com.spotlyric.app.data.remote.gemini;

import com.spotlyric.app.data.local.datastore.SettingsPreferences;
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
  private final Provider<SettingsPreferences> settingsPreferencesProvider;

  public GeminiLyricsService_Factory(Provider<SettingsPreferences> settingsPreferencesProvider) {
    this.settingsPreferencesProvider = settingsPreferencesProvider;
  }

  @Override
  public GeminiLyricsService get() {
    return newInstance(settingsPreferencesProvider.get());
  }

  public static GeminiLyricsService_Factory create(
      Provider<SettingsPreferences> settingsPreferencesProvider) {
    return new GeminiLyricsService_Factory(settingsPreferencesProvider);
  }

  public static GeminiLyricsService newInstance(SettingsPreferences settingsPreferences) {
    return new GeminiLyricsService(settingsPreferences);
  }
}
