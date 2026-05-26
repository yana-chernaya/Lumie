package com.yanachernaya.lumie.data.repository

import com.yanachernaya.lumie.domain.entity.AppTheme
import com.yanachernaya.lumie.domain.entity.Category
import com.yanachernaya.lumie.domain.entity.Interval
import com.yanachernaya.lumie.domain.entity.PresetBackground
import com.yanachernaya.lumie.domain.entity.Settings
import com.yanachernaya.lumie.domain.entity.UserGender
import com.yanachernaya.lumie.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class SettingsRepositoryImpl @Inject constructor() : SettingsRepository {

    override fun getSelectedCategory(): Flow<Category> {
        TODO("Not yet implemented")
    }

    override fun getSettings(): Flow<Settings> {
        TODO("Not yet implemented")
    }

    override suspend fun selectCategory(category: Category) {
        TODO("Not yet implemented")
    }

    override suspend fun updateUserGender(userGender: UserGender) {
        TODO("Not yet implemented")
    }

    override suspend fun updateInterval(interval: Interval) {
        TODO("Not yet implemented")
    }

    override suspend fun updateAppTheme(appTheme: AppTheme) {
        TODO("Not yet implemented")
    }

    override suspend fun updateMoodBackgroundEnabled(enabled: Boolean) {
        TODO("Not yet implemented")
    }

    override suspend fun updatePresetBackground(presetBackground: PresetBackground) {
        TODO("Not yet implemented")
    }

    override suspend fun updateWifiOnly(wifiOnly: Boolean) {
        TODO("Not yet implemented")
    }

    override suspend fun updateNotificationsEnabled(enabled: Boolean) {
        TODO("Not yet implemented")
    }
}