package com.yanachernaya.lumie.di

import com.yanachernaya.lumie.data.repository.SettingsRepositoryImpl
import com.yanachernaya.lumie.domain.repository.SettingsRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
interface DataModule {

    @Binds
    @Singleton
    fun bindSettingsRepository(
        impl: SettingsRepositoryImpl
    ): SettingsRepository
}