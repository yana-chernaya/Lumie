package com.yanachernaya.lumie.data.mapper

import com.yanachernaya.lumie.data.local.database.AffirmationDbModel
import com.yanachernaya.lumie.domain.entity.Affirmation

fun AffirmationDbModel.toEntity(): Affirmation =
    Affirmation(
        id = id,
        text = text,
        imageUrl = imageUrl,
        category = category,
        isFavorite = isFavorite,
        createdAt = createdAt
    )

fun List<AffirmationDbModel>.toEntities(): List<Affirmation> = map { it.toEntity() }