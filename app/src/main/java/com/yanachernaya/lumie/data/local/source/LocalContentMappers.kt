package com.yanachernaya.lumie.data.local.source

import com.yanachernaya.lumie.R
import com.yanachernaya.lumie.domain.entity.Category
import com.yanachernaya.lumie.domain.entity.PresetBackground


fun PresetBackground.toDrawableId() : Int = when (this) {
    PresetBackground.PURPLE_SEA -> R.drawable.bg_purple_sea
    PresetBackground.PINK_CLOUDS -> R.drawable.bg_pink_clouds
    PresetBackground.MISTY_FLOWERS -> R.drawable.bg_misty_flowers
    PresetBackground.PURPLE_DEW_LEAVES -> R.drawable.bg_purple_dew_leaves
    PresetBackground.RED_BLUE_GALAXY_PLANET -> R.drawable.bg_red_blue_galaxy_planet
    PresetBackground.BLUE_COSMIC_PLANET -> R.drawable.bg_blue_cosmic_planet
    PresetBackground.FOGGY_BRIDGE -> R.drawable.bg_foggy_bridge
    PresetBackground.FOREST_SUNBEAM_ROAD -> R.drawable.bg_forest_sunbeam_road
    PresetBackground.STARRY_MOUNTAIN_LAKE -> R.drawable.bg_starry_mountain_lake
    PresetBackground.MILKY_WAY_OCEAN_PIER -> R.drawable.bg_milky_way_ocean_pier
    PresetBackground.PINK_MISTY_MOUNTAIN -> R.drawable.bg_pink_misty_mountain
    PresetBackground.FOREST_LAKE -> R.drawable.bg_forest_lake
    PresetBackground.PURPLE_KNIT -> R.drawable.bg_purple_knit
    PresetBackground.SUNSET_GRASS -> R.drawable.bg_sunset_grass
    PresetBackground.FLUFFY_HEARTS -> R.drawable.bg_fluffy_hearts
    PresetBackground.PINK_FEATHERS -> R.drawable.bg_pink_feathers
    PresetBackground.PINK_GYPSOPHILA -> R.drawable.bg_pink_gypsophila
    PresetBackground.NEON_CITRUS -> R.drawable.bg_neon_citrus
    PresetBackground.PURPLE_STARRY_LAKE -> R.drawable.bg_purple_starry_lake
    PresetBackground.ZEN_STONES -> R.drawable.bg_zen_stones
    PresetBackground.COUPLE_SUNSET -> R.drawable.bg_couple_sunset
    PresetBackground.PURPLE_MEADOW -> R.drawable.bg_purple_meadow
    PresetBackground.MOON_WATERFALL -> R.drawable.bg_moon_waterfall
    PresetBackground.DARK_LAKE -> R.drawable.bg_dark_lake
    PresetBackground.PINE_DEW -> R.drawable.bg_pine_dew
    PresetBackground.GREEN_PEAK_ROAD -> R.drawable.bg_green_peak_road
    PresetBackground.FOREST_VALLEY -> R.drawable.bg_forest_valley
    PresetBackground.PURPLE_NIGHT_BEACH -> R.drawable.bg_purple_night_beach
    PresetBackground.COSMIC_BEACH -> R.drawable.bg_cosmic_beach
    PresetBackground.NIGHT_CLOUDS -> R.drawable.bg_night_clouds
    PresetBackground.STARDUST_MOON -> R.drawable.bg_stardust_moon
    PresetBackground.VIOLET_NEBULA -> R.drawable.bg_violet_nebula
    PresetBackground.STARRY_CLOUDS -> R.drawable.bg_starry_clouds
    PresetBackground.DARK_LEAVES -> R.drawable.bg_dark_leaves
    PresetBackground.AUTUMN_WINDOW -> R.drawable.bg_autumn_window
    PresetBackground.CITY_LIGHTS -> R.drawable.bg_city_lights
}

fun Category.toJsonKey(): String = when(this) {
    Category.SELF_LOVE -> "self_love"
    Category.MOTIVATION -> "motivation"
    Category.HAPPINESS -> "happiness"
    Category.CALM -> "calm"
    Category.HEALTH -> "health"
    Category.SUCCESS -> "success"
    Category.RELATIONSHIP -> "relationship"
    Category.CONFIDENCE -> "confidence"
    Category.ENERGY -> "energy"
    Category.GRATITUDE -> "gratitude"
}