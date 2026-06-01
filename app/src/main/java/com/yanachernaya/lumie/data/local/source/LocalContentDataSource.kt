package com.yanachernaya.lumie.data.local.source

import com.yanachernaya.lumie.domain.entity.Category
import com.yanachernaya.lumie.domain.entity.PresetBackground

interface LocalContentDataSource {

    suspend fun getRandomText(category: Category): String

    fun getRandomBackgroundUriString(): String

    fun getPresetBackgroundUriString(presetBackground: PresetBackground): String
}

