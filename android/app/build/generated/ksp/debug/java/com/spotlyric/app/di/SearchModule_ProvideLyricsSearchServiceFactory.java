package com.spotlyric.app.di;

import com.spotlyric.app.data.local.datastore.SettingsPreferences;
import com.spotlyric.app.data.remote.serpapi.LyricsSearchService;
import com.spotlyric.app.data.remote.serpapi.SerpApiService;
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
public final class SearchModule_ProvideLyricsSearchServiceFactory implements Factory<LyricsSearchService> {
  private final Provider<SerpApiService> serpApiServiceProvider;

  private final Provider<SettingsPreferences> settingsPreferencesProvider;

  public SearchModule_ProvideLyricsSearchServiceFactory(
      Provider<SerpApiService> serpApiServiceProvider,
      Provider<SettingsPreferences> settingsPreferencesProvider) {
    this.serpApiServiceProvider = serpApiServiceProvider;
    this.settingsPreferencesProvider = settingsPreferencesProvider;
  }

  @Override
  public LyricsSearchService get() {
    return provideLyricsSearchService(serpApiServiceProvider.get(), settingsPreferencesProvider.get());
  }

  public static SearchModule_ProvideLyricsSearchServiceFactory create(
      Provider<SerpApiService> serpApiServiceProvider,
      Provider<SettingsPreferences> settingsPreferencesProvider) {
    return new SearchModule_ProvideLyricsSearchServiceFactory(serpApiServiceProvider, settingsPreferencesProvider);
  }

  public static LyricsSearchService provideLyricsSearchService(SerpApiService serpApiService,
      SettingsPreferences settingsPreferences) {
    return Preconditions.checkNotNullFromProvides(SearchModule.INSTANCE.provideLyricsSearchService(serpApiService, settingsPreferences));
  }
}
