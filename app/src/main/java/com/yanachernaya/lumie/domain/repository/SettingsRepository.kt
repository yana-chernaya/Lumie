package com.yanachernaya.lumie.domain.repository

import com.yanachernaya.lumie.domain.entity.AppTheme
import com.yanachernaya.lumie.domain.entity.Category
import com.yanachernaya.lumie.domain.entity.Interval
import com.yanachernaya.lumie.domain.entity.PresetBackground
import com.yanachernaya.lumie.domain.entity.Settings
import com.yanachernaya.lumie.domain.entity.UserGender
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {

    fun getSelectedCategory(): Flow<Category>

    fun getSettings(): Flow<Settings>

    suspend fun selectCategory(category: Category)

    suspend fun updateUserGender(userGender: UserGender)

    suspend fun updateInterval(interval: Interval)

    suspend fun updateAppTheme(appTheme: AppTheme)

    suspend fun updateMoodBackgroundEnabled(enabled: Boolean)

    suspend fun updatePresetBackground(presetBackground: PresetBackground)

    suspend fun updateWifiOnly(wifiOnly: Boolean)

    suspend fun updateNotificationsEnabled(enabled: Boolean)
}