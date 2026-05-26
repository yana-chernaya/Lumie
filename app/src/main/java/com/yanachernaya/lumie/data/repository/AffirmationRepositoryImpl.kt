package com.yanachernaya.lumie.data.repository

import com.yanachernaya.lumie.domain.entity.Affirmation
import com.yanachernaya.lumie.domain.entity.Category
import com.yanachernaya.lumie.domain.entity.PresetBackground
import com.yanachernaya.lumie.domain.entity.UserGender
import com.yanachernaya.lumie.domain.repository.AffirmationRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class AffirmationRepositoryImpl @Inject constructor() : AffirmationRepository {
    override fun getAllAffirmations(): Flow<List<Affirmation>> {
        TODO("Not yet implemented")
    }

    override fun getFavorites(): Flow<List<Affirmation>> {
        TODO("Not yet implemented")
    }

    override fun getAffirmationById(id: Int): Flow<Affirmation> {
        TODO("Not yet implemented")
    }

    override suspend fun toggleFavorite(id: Int) {
        TODO("Not yet implemented")
    }

    override suspend fun loadNewAffirmation(
        category: Category,
        userGender: UserGender,
        presetBackground: PresetBackground?
    ): Affirmation {
        TODO("Not yet implemented")
    }

    override suspend fun changeBackground(
        id: Int,
        presetBackground: PresetBackground
    ) {
        TODO("Not yet implemented")
    }
}