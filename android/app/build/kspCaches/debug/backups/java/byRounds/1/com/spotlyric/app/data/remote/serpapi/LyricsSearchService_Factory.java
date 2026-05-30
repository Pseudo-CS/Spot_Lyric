package com.spotlyric.app.data.remote.serpapi;

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
public final class LyricsSearchService_Factory implements Factory<LyricsSearchService> {
  private final Provider<SerpApiService> serpApiServiceProvider;

  private final Provider<SettingsPreferences> settingsPreferencesProvider;

  public LyricsSearchService_Factory(Provider<SerpApiService> serpApiServiceProvider,
      Provider<SettingsPreferences> settingsPreferencesProvider) {
    this.serpApiServiceProvider = serpApiServiceProvider;
    this.settingsPreferencesProvider = settingsPreferencesProvider;
  }

  @Override
  public LyricsSearchService get() {
    return newInstance(serpApiServiceProvider.get(), settingsPreferencesProvider.get());
  }

  public static LyricsSearchService_Factory create(Provider<SerpApiService> serpApiServiceProvider,
      Provider<SettingsPreferences> settingsPreferencesProvider) {
    return new LyricsSearchService_Factory(serpApiServiceProvider, settingsPreferencesProvider);
  }

  public static LyricsSearchService newInstance(SerpApiService serpApiService,
      SettingsPreferences settingsPreferences) {
    return new LyricsSearchService(serpApiService, settingsPreferences);
  }
}
