package com.spotlyric.app.di

import android.content.Context
import com.spotlyric.app.data.local.datastore.AuthPreferences
import com.spotlyric.app.data.local.datastore.SettingsPreferences
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DataStoreModule {

    @Provides
    @Singleton
    fun provideAuthPreferences(@ApplicationContext context: Context): AuthPreferences {
        return AuthPreferences(context)
    }

    @Provides
    @Singleton
    fun provideSettingsPreferences(@ApplicationContext context: Context): SettingsPreferences {
        return SettingsPreferences(context)
    }
}
