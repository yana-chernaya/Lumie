package com.yanachernaya.lumie.domain.entity

data class Affirmation(
    val id: Int = 0,
    val text: String,
    val imageUrl: String,
    val category: Category,
    val isFavorite: Boolean = false,
    val createdAt: Long
)