package com.spotlyric.app.di

import com.spotlyric.app.data.local.datastore.SettingsPreferences
import com.spotlyric.app.data.remote.gemini.GeminiLyricsService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object GeminiModule {

    @Provides
    @Singleton
    fun provideGeminiLyricsService(settingsPreferences: SettingsPreferences): GeminiLyricsService {
        return GeminiLyricsService(settingsPreferences = settingsPreferences)
    }
}


