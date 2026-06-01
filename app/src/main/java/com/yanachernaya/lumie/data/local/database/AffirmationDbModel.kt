package com.yanachernaya.lumie.data.local.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.yanachernaya.lumie.domain.entity.Category

@Entity(tableName = "affirmations")
data class AffirmationDbModel(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val text: String,
    val imageUrl: String,
    val category: Category,
    val isFavorite: Boolean = false,
    val createdAt: Long
)
