package com.yanachernaya.lumie.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.IOException
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.yanachernaya.lumie.data.mapper.toInterval
import com.yanachernaya.lumie.domain.entity.AppTheme
import com.yanachernaya.lumie.domain.entity.Category
import com.yanachernaya.lumie.domain.entity.Interval
import com.yanachernaya.lumie.domain.entity.PresetBackground
import com.yanachernaya.lumie.domain.entity.Settings
import com.yanachernaya.lumie.domain.entity.UserGender
import com.yanachernaya.lumie.domain.repository.SettingsRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import javax.inject.Inject

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class SettingsRepositoryImpl @Inject constructor(
    @param:ApplicationContext private val context: Context
) : SettingsRepository {

    private inline fun <reified T : Enum<T>> safeEnumValueOf(name: String?, default: T): T {
        return try {
            if (name == null) default else enumValueOf<T>(name)
        } catch (_: IllegalArgumentException) {
            default
        }
    }

    override fun getSelectedCategory(): Flow<Category> =
        context.dataStore.data
            .catch { exception ->
                if (exception is IOException) emit(emptyPreferences()) else throw exception
            }
            .map { preferences ->
                val categoryId = preferences[CATEGORY_KEY] ?: Settings.DEFAULT_CATEGORY.id
                Category.fromId(categoryId)
            }
            .distinctUntilChanged()

    override fun getSettings(): Flow<Settings> =
        context.dataStore.data
            .catch { exception ->
                if (exception is IOException) emit(emptyPreferences()) else throw exception
            }
            .map { preferences ->
                val categoryId = preferences[CATEGORY_KEY] ?: Settings.DEFAULT_CATEGORY.id
                val category = Category.fromId(categoryId)

                val userGender = safeEnumValueOf(
                    preferences[USER_GENDER_KEY],
                    Settings.DEFAULT_USER_GENDER
                )

                val interval = preferences[INTERVAL_KEY]?.toInterval()
                    ?: Settings.DEFAULT_INTERVAL

                val appTheme = safeEnumValueOf(
                    preferences[APP_THEME_KEY],
                    Settings.DEFAULT_APP_THEME
                )

                val isMoodBackgroundEnabled =
                    preferences[MOOD_BACKGROUND_ENABLED_KEY]
                        ?: Settings.DEFAULT_IS_MOOD_BACKGROUND_ENABLED
                val presetBackground = safeEnumValueOf(
                    preferences[PRESET_BACKGROUND_KEY],
                    Settings.DEFAULT_PRESET_BACKGROUND
                )

                val isWifiOnly = preferences[WIFI_ONLY_KEY]
                    ?: Settings.DEFAULT_IS_WIFI_ONLY

                val isNotificationsEnabled = preferences[NOTIFICATIONS_ENABLED_KEY]
                    ?: Settings.DEFAULT_IS_NOTIFICATIONS_ENABLED

                Settings(
                    category = category,
                    userGender = userGender,
                    interval = interval,
                    appTheme = appTheme,
                    isMoodBackgroundEnabled = isMoodBackgroundEnabled,
                    presetBackground = presetBackground,
                    isWifiOnly = isWifiOnly,
                    isNotificationsEnabled = isNotificationsEnabled
                )
            }
            .distinctUntilChanged()

    private suspend fun <T> save(key: Preferences.Key<T>, value: T) {
        context.dataStore.edit { preferences ->
            preferences[key] = value
        }
    }

    override suspend fun selectCategory(category: Category) =
        save(CATEGORY_KEY, category.id)

    override suspend fun updateUserGender(userGender: UserGender) =
        save(USER_GENDER_KEY, userGender.name)

    override suspend fun updateInterval(interval: Interval) =
        save(INTERVAL_KEY, interval.minutes)

    override suspend fun updateAppTheme(appTheme: AppTheme) =
        save(APP_THEME_KEY, appTheme.name)

    override suspend fun updateMoodBackgroundEnabled(enabled: Boolean) =
        save(MOOD_BACKGROUND_ENABLED_KEY, enabled)

    override suspend fun updatePresetBackground(presetBackground: PresetBackground) =
        save(PRESET_BACKGROUND_KEY, presetBackground.name)

    override suspend fun updateWifiOnly(wifiOnly: Boolean) =
        save(WIFI_ONLY_KEY, wifiOnly)

    override suspend fun updateNotificationsEnabled(enabled: Boolean) =
        save(NOTIFICATIONS_ENABLED_KEY, enabled)

    companion object {
        private val CATEGORY_KEY = stringPreferencesKey("category")
        private val USER_GENDER_KEY = stringPreferencesKey("user_gender")
        private val INTERVAL_KEY = intPreferencesKey("interval")
        private val APP_THEME_KEY = stringPreferencesKey("app_theme")
        private val MOOD_BACKGROUND_ENABLED_KEY = booleanPreferencesKey("mood_background_enabled")
        private val PRESET_BACKGROUND_KEY = stringPreferencesKey("preset_background")
        private val WIFI_ONLY_KEY = booleanPreferencesKey("wifi_only")
        private val NOTIFICATIONS_ENABLED_KEY = booleanPreferencesKey("notifications_enabled")
    }
}