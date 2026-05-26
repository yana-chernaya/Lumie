package com.yanachernaya.lumie.domain.entity

enum class Category(
    val id: String,
    val keywords: String
) {
    SELF_LOVE(
        id = "self_love",
        keywords = "light aesthetic flowers soft textures minimalist wallpaper"
    ),
    MOTIVATION(
        id = "motivation",
        keywords = "motivation freedom mountain top open road landscape sky sunrise nature wallpaper"
    ),
    HAPPINESS(
        id = "happiness",
        keywords = "color happiness wallpaper"
    ),
    CALM(
        id = "calm",
        keywords = "calm water wallpaper sunset beach"
    ),
    HEALTH(
        id = "health",
        keywords = "fresh water on green vibrant green"
    ),
    SUCCESS(
        id = "success",
        keywords = "abstract golden light growth nature wisdom life"
    ),
    RELATIONSHIP(
        id = "relationship",
        keywords = "soft gradients pastel colors warm pink peach tones"
    ),
    CONFIDENCE(
        id = "confidence",
        keywords = "abstract mountain shapes rock textures stable foundation unwavering mood dark"
    ),
    ENERGY(
        id = "energy",
        keywords = "waterfall wallpaper stars"
    ),
    GRATITUDE(
        id = "gratitude",
        keywords = "galaxy space wallpaper"
    );

    companion object {

        val DEFAULT = SELF_LOVE

        fun fromId(id: String): Category {
            return entries.find { it.id == id } ?: DEFAULT
        }
    }
}