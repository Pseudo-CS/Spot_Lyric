package com.spotlyric.app.di;

import android.content.Context;
import com.spotlyric.app.data.local.datastore.SettingsPreferences;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata("javax.inject.Singleton")
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
public final class DataStoreModule_ProvideSettingsPreferencesFactory implements Factory<SettingsPreferences> {
  private final Provider<Context> contextProvider;

  public DataStoreModule_ProvideSettingsPreferencesFactory(Provider<Context> contextProvider) {
    this.contextProvider = contextProvider;
  }

  @Override
  public SettingsPreferences get() {
    return provideSettingsPreferences(contextProvider.get());
  }

  public static DataStoreModule_ProvideSettingsPreferencesFactory create(
      Provider<Context> contextProvider) {
    return new DataStoreModule_ProvideSettingsPreferencesFactory(contextProvider);
  }

  public static SettingsPreferences provideSettingsPreferences(Context context) {
    return Preconditions.checkNotNullFromProvides(DataStoreModule.INSTANCE.provideSettingsPreferences(context));
  }
}
