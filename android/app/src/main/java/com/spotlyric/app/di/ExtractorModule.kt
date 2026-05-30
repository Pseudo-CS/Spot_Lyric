package com.spotlyric.app.di

import com.spotlyric.app.data.remote.extractor.LyricsExtractorService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ExtractorModule {

    @Provides
    @Singleton
    fun provideLyricsExtractorService(okHttpClient: OkHttpClient): LyricsExtractorService {
        return LyricsExtractorService(okHttpClient)
    }
}
