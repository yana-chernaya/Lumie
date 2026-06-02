package com.yanachernaya.lumie.data.mapper

import com.yanachernaya.lumie.domain.entity.Category
import com.yanachernaya.lumie.domain.entity.UserGender

fun Category.toApiTopic(): String = when (this) {
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

fun UserGender.toApiGender(): String = when (this) {
    UserGender.MALE -> "male"
    UserGender.FEMALE -> "female"
}