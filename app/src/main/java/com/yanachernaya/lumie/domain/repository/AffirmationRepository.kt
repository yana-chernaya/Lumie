package com.yanachernaya.lumie.domain.repository

import com.yanachernaya.lumie.domain.entity.Affirmation
import com.yanachernaya.lumie.domain.entity.Category
import com.yanachernaya.lumie.domain.entity.PresetBackground
import com.yanachernaya.lumie.domain.entity.UserGender
import kotlinx.coroutines.flow.Flow

interface AffirmationRepository {

    fun getAllAffirmations(): Flow<List<Affirmation>>

    fun getFavorites(): Flow<List<Affirmation>>

    fun getAffirmationById(id: Int): Flow<Affirmation>

    suspend fun toggleFavorite(id: Int)

    suspend fun loadNewAffirmation(
        category: Category,
        userGender: UserGender,
        presetBackground: PresetBackground? = null
    ): Affirmation

    suspend fun changeBackground(
        id: Int,
        presetBackground: PresetBackground
    )
}