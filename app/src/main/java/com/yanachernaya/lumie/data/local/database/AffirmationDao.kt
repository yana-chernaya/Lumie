package com.yanachernaya.lumie.data.local.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface AffirmationDao {

    @Query("SELECT * FROM affirmations ORDER BY createdAt DESC")
    fun getAllAffirmations(): Flow<List<AffirmationDbModel>>

    @Query("SELECT * FROM affirmations WHERE isFavorite = 1 ORDER BY createdAt DESC")
    fun getFavorites(): Flow<List<AffirmationDbModel>>

    @Query("SELECT * FROM affirmations WHERE id = :id LIMIT 1")
    fun getAffirmationById(id: Int): Flow<AffirmationDbModel>

    @Query("UPDATE affirmations SET isFavorite = NOT isFavorite WHERE id = :id")
    suspend fun toggleFavorite(id: Int)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAffirmation(affirmationDbModel: AffirmationDbModel): Long

    @Query("UPDATE affirmations SET imageUrl = :imageUrl WHERE id = :id AND imageUrl != :imageUrl")
    suspend fun updateBackground(id: Int, imageUrl: String)
}