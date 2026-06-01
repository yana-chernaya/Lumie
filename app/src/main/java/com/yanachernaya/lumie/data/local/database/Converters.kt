package com.yanachernaya.lumie.data.local.database

import androidx.room.TypeConverter
import com.yanachernaya.lumie.domain.entity.Category

class Converters {

    @TypeConverter
    fun fromCategory(category: Category): String {
        return category.id
    }

    @TypeConverter
    fun toCategory(id: String): Category {
        return Category.fromId(id)
    }
}