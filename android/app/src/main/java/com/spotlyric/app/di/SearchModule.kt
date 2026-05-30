package com.spotlyric.app.di

import com.spotlyric.app.data.local.datastore.SettingsPreferences
import com.spotlyric.app.data.remote.serpapi.LyricsSearchService
import com.spotlyric.app.data.remote.serpapi.SerpApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object SearchModule {

    @Provides
    @Singleton
    fun provideLyricsSearchService(
        serpApiService: SerpApiService,
        settingsPreferences: SettingsPreferences
    ): LyricsSearchService {
        return LyricsSearchService(serpApiService, settingsPreferences)
    }
}
