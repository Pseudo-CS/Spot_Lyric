package com.spotlyric.app.data.repository;

import android.content.Context;
import com.spotlyric.app.data.local.datastore.AuthPreferences;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
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
public final class AuthRepositoryImpl_Factory implements Factory<AuthRepositoryImpl> {
  private final Provider<Context> contextProvider;

  private final Provider<AuthPreferences> authPreferencesProvider;

  public AuthRepositoryImpl_Factory(Provider<Context> contextProvider,
      Provider<AuthPreferences> authPreferencesProvider) {
    this.contextProvider = contextProvider;
    this.authPreferencesProvider = authPreferencesProvider;
  }

  @Override
  public AuthRepositoryImpl get() {
    return newInstance(contextProvider.get(), authPreferencesProvider.get());
  }

  public static AuthRepositoryImpl_Factory create(Provider<Context> contextProvider,
      Provider<AuthPreferences> authPreferencesProvider) {
    return new AuthRepositoryImpl_Factory(contextProvider, authPreferencesProvider);
  }

  public static AuthRepositoryImpl newInstance(Context context, AuthPreferences authPreferences) {
    return new AuthRepositoryImpl(context, authPreferences);
  }
}
