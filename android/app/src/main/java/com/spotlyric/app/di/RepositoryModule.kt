package com.spotlyric.app.di

import com.spotlyric.app.data.repository.AuthRepositoryImpl
import com.spotlyric.app.data.repository.ManageRepositoryImpl
import com.spotlyric.app.data.repository.PlayerRepositoryImpl
import com.spotlyric.app.data.repository.SourcesRepositoryImpl
import com.spotlyric.app.domain.repository.AuthRepository
import com.spotlyric.app.domain.repository.ManageRepository
import com.spotlyric.app.domain.repository.PlayerRepository
import com.spotlyric.app.domain.repository.SourcesRepository
import com.spotlyric.app.data.repository.BackupRepositoryImpl
import com.spotlyric.app.domain.repository.BackupRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindAuthRepository(impl: AuthRepositoryImpl): AuthRepository

    @Binds
    @Singleton
    abstract fun bindPlayerRepository(impl: PlayerRepositoryImpl): PlayerRepository

    @Binds
    @Singleton
    abstract fun bindManageRepository(impl: ManageRepositoryImpl): ManageRepository

    @Binds
    @Singleton
    abstract fun bindSourcesRepository(impl: SourcesRepositoryImpl): SourcesRepository

    @Binds
    @Singleton
    abstract fun bindBackupRepository(impl: BackupRepositoryImpl): BackupRepository
}
