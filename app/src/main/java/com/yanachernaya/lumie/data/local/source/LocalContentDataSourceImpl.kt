package com.yanachernaya.lumie.data.local.source

import android.content.Context
import com.yanachernaya.lumie.domain.entity.Category
import com.yanachernaya.lumie.domain.entity.PresetBackground
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import javax.inject.Inject

class LocalContentDataSourceImpl @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val json: Json
) : LocalContentDataSource {

    private val fallbackTexts: FallbackAffirmationsDto by lazy {
        val jsonString = context.assets.open("fallback_affirmations.json")
            .bufferedReader()
            .use { it.readText() }

        json.decodeFromString<FallbackAffirmationsDto>(jsonString)
    }

    override suspend fun getRandomText(category: Category): String =
        withContext(Dispatchers.IO) {
            val categoryKey = category.toJsonKey()
            val affirmations = fallbackTexts.affirmations[categoryKey]
                ?: error("No fallback affirmations found for category key: $categoryKey")

            check(affirmations.isNotEmpty()) {
                "Fallback affirmations list is empty for category key: $categoryKey"
            }

            affirmations.random()
        }

    override fun getRandomBackgroundUriString(): String {
        val randomEnum = PresetBackground.entries.random()
        return getPresetBackgroundUriString(randomEnum)
    }

    override fun getPresetBackgroundUriString(presetBackground: PresetBackground): String {
        val drawableResId = presetBackground.toDrawableId()
        return "android.resource://${context.packageName}/$drawableResId"
    }
}

@Serializable
data class FallbackAffirmationsDto(
    val affirmations: Map<String, List<String>>
)