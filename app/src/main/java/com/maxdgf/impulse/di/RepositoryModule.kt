package com.maxdgf.impulse.di

import com.maxdgf.impulse.data.impl.AudioRepositoryImpl
import com.maxdgf.impulse.data.impl.MediaControllerRepositoryImpl
import com.maxdgf.impulse.domain.repository.AudioRepository
import com.maxdgf.impulse.domain.repository.MediaControllerRepository
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
    abstract fun bindAudioRepository(impl: AudioRepositoryImpl): AudioRepository

    @Binds
    @Singleton
    abstract fun bindMediaControllerRepository(impl: MediaControllerRepositoryImpl) : MediaControllerRepository
}