package com.yanachernaya.lumie.data.repository

import android.util.Log
import com.yanachernaya.lumie.data.local.database.AffirmationDao
import com.yanachernaya.lumie.data.local.database.AffirmationDbModel
import com.yanachernaya.lumie.data.local.source.LocalContentDataSource
import com.yanachernaya.lumie.data.mapper.toApiGender
import com.yanachernaya.lumie.data.mapper.toApiTopic
import com.yanachernaya.lumie.data.mapper.toEntities
import com.yanachernaya.lumie.data.mapper.toEntity
import com.yanachernaya.lumie.data.remote.GenerateAffirmationRequest
import com.yanachernaya.lumie.data.remote.LumieApiService
import com.yanachernaya.lumie.domain.entity.Affirmation
import com.yanachernaya.lumie.domain.entity.Category
import com.yanachernaya.lumie.domain.entity.PresetBackground
import com.yanachernaya.lumie.domain.entity.UserGender
import com.yanachernaya.lumie.domain.repository.AffirmationRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject

private const val TAG = "AffirmationRepositoryImpl"

class AffirmationRepositoryImpl @Inject constructor(
    private val affirmationDao: AffirmationDao,
    private val apiService: LumieApiService,
    private val localContentDataSource: LocalContentDataSource
) : AffirmationRepository {

    override fun getAllAffirmations(): Flow<List<Affirmation>> =
        affirmationDao.getAllAffirmations().map { it.toEntities() }

    override fun getFavorites(): Flow<List<Affirmation>> =
        affirmationDao.getFavorites().map { it.toEntities() }

    override fun getAffirmationById(id: Int): Flow<Affirmation> =
        affirmationDao.getAffirmationById(id).map { it.toEntity() }

    override suspend fun toggleFavorite(id: Int) {
        affirmationDao.toggleFavorite(id)
    }

    override suspend fun loadNewAffirmation(
        category: Category,
        userGender: UserGender,
        presetBackground: PresetBackground?
    ): Affirmation = withContext(Dispatchers.IO) {
        val (text, unsplashQuery) = fetchTextAndQuery(category, userGender)
        val imageUrlString = fetchBackgroundUrl(presetBackground, unsplashQuery)

        val newDbModel = AffirmationDbModel(
            text = text,
            imageUrl = imageUrlString,
            category = category,
            isFavorite = false,
            createdAt = System.currentTimeMillis()
        )

        val newId = affirmationDao.insertAffirmation(newDbModel)
        newDbModel.toEntity().copy(id = newId.toInt())
    }

    private suspend fun fetchTextAndQuery(
        category: Category,
        userGender: UserGender
    ): Pair<String, String> {
        val request = GenerateAffirmationRequest(
            category = category.toApiTopic(),
            userGender = userGender.toApiGender()
        )

        return try {
            val response = apiService.generateAffirmation(request)
            val text = response.data.affirmation
            val unsplashQuery = response.data.unsplashQuery

            when {
                text.isBlank() ->
                    localContentDataSource.getRandomText(category) to category.keywords

                text.isNotBlank() && unsplashQuery.isBlank() ->
                    text to category.keywords

                else ->
                    text to unsplashQuery
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Error loading new affirmation", e)
            localContentDataSource.getRandomText(category) to category.keywords
        }
    }

    private suspend fun fetchBackgroundUrl(
        presetBackground: PresetBackground?,
        unsplashQuery: String
    ): String {
        presetBackground?.let {
            return localContentDataSource.getPresetBackgroundUriString(it)
        }
        return try {
            val response = apiService.getBackgroundImage(unsplashQuery)
            val imageUrl = response.data.imageUrl

            imageUrl.ifBlank {
                localContentDataSource.getRandomBackgroundUriString()
            }

        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Error loading background", e)
            localContentDataSource.getRandomBackgroundUriString()
        }
    }

    override suspend fun changeBackground(
        id: Int,
        presetBackground: PresetBackground
    ) {
        val imageUrl = localContentDataSource.getPresetBackgroundUriString(presetBackground)
        affirmationDao.updateBackground(id, imageUrl)
    }
}