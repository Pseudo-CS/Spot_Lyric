package com.spotlyric.app.presentation.settings;

import android.content.Context;
import com.spotlyric.app.data.local.datastore.SettingsPreferences;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata
@QualifierMetadata("dagger.hilt.android.qualifiers.ApplicationContext")
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
public final class SettingsViewModel_Factory implements Factory<SettingsViewModel> {
  private final Provider<SettingsPreferences> settingsPreferencesProvider;

  private final Provider<Context> contextProvider;

  public SettingsViewModel_Factory(Provider<SettingsPreferences> settingsPreferencesProvider,
      Provider<Context> contextProvider) {
    this.settingsPreferencesProvider = settingsPreferencesProvider;
    this.contextProvider = contextProvider;
  }

  @Override
  public SettingsViewModel get() {
    return newInstance(settingsPreferencesProvider.get(), contextProvider.get());
  }

  public static SettingsViewModel_Factory create(
      Provider<SettingsPreferences> settingsPreferencesProvider,
      Provider<Context> contextProvider) {
    return new SettingsViewModel_Factory(settingsPreferencesProvider, contextProvider);
  }

  public static SettingsViewModel newInstance(SettingsPreferences settingsPreferences,
      Context context) {
    return new SettingsViewModel(settingsPreferences, context);
  }
}
