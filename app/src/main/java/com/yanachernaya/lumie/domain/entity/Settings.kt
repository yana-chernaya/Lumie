package com.yanachernaya.lumie.domain.entity

data class Settings(
    val category: Category,
    val userGender: UserGender,
    val interval: Interval,
    val appTheme: AppTheme,
    val isMoodBackgroundEnabled: Boolean,
    val presetBackground: PresetBackground,
    val isWifiOnly: Boolean,
    val isNotificationsEnabled: Boolean
) {

    companion object {
        val DEFAULT_CATEGORY = Category.DEFAULT
        val DEFAULT_USER_GENDER = UserGender.FEMALE
        val DEFAULT_INTERVAL = Interval.HOURS_6
        val DEFAULT_APP_THEME = AppTheme.SYSTEM
        const val DEFAULT_IS_MOOD_BACKGROUND_ENABLED = true
        val DEFAULT_PRESET_BACKGROUND = PresetBackground.PURPLE_SEA
        const val DEFAULT_IS_WIFI_ONLY = true
        const val DEFAULT_IS_NOTIFICATIONS_ENABLED = false
    }
}

enum class UserGender {
    FEMALE, MALE
}

enum class Interval(val minutes: Int) {
    HOURS_6(360),
    HOURS_12(720),
    HOURS_24(1440)
}

enum class AppTheme {
    LIGHT, DARK, SYSTEM
}

enum class PresetBackground {
    PURPLE_SEA,
    PINK_CLOUDS,
    MISTY_FLOWERS,
    PURPLE_DEW_LEAVES,
    RED_BLUE_GALAXY_PLANET,
    BLUE_COSMIC_PLANET,
    FOGGY_BRIDGE,
    FOREST_SUNBEAM_ROAD,
    STARRY_MOUNTAIN_LAKE,
    MILKY_WAY_OCEAN_PIER,
    PINK_MISTY_MOUNTAIN,
    FOREST_LAKE,
    PURPLE_KNIT,
    SUNSET_GRASS,
    FLUFFY_HEARTS,
    PINK_FEATHERS,
    PINK_GYPSOPHILA,
    NEON_CITRUS,
    PURPLE_STARRY_LAKE,
    ZEN_STONES,
    COUPLE_SUNSET,
    PURPLE_MEADOW,
    MOON_WATERFALL,
    DARK_LAKE,
    PINE_DEW,
    GREEN_PEAK_ROAD,
    FOREST_VALLEY,
    PURPLE_NIGHT_BEACH,
    COSMIC_BEACH,
    NIGHT_CLOUDS,
    STARDUST_MOON,
    VIOLET_NEBULA,
    STARRY_CLOUDS,
    DARK_LEAVES,
    AUTUMN_WINDOW,
    CITY_LIGHTS
}