package com.yanachernaya.lumie.di

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.yanachernaya.lumie.R
import com.yanachernaya.lumie.data.local.database.AffirmationDao
import com.yanachernaya.lumie.data.local.database.AppDatabase
import com.yanachernaya.lumie.data.repository.AffirmationRepositoryImpl
import com.yanachernaya.lumie.data.repository.ImageRepositoryImpl
import com.yanachernaya.lumie.data.repository.SettingsRepositoryImpl
import com.yanachernaya.lumie.domain.repository.AffirmationRepository
import com.yanachernaya.lumie.domain.repository.ImageRepository
import com.yanachernaya.lumie.domain.repository.SettingsRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
interface DataModule {

    @Binds
    @Singleton
    fun bindSettingsRepository(
        impl: SettingsRepositoryImpl
    ): SettingsRepository

    @Binds
    @Singleton
    fun bindAffirmationRepository(
        impl: AffirmationRepositoryImpl
    ): AffirmationRepository

    @Binds
    @Singleton
    fun bindImageRepository(
        impl: ImageRepositoryImpl
    ): ImageRepository

    companion object {
        private const val DATABASE_NAME = "lumie_app.db"

        @Provides
        @Singleton
        fun provideAppDatabase(
            @ApplicationContext context: Context
        ): AppDatabase {
            return Room.databaseBuilder(
                context = context,
                name = DATABASE_NAME,
                klass = AppDatabase::class.java
            )
                .fallbackToDestructiveMigration(dropAllTables = true)
                .addCallback(object : RoomDatabase.Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)

                        val affirmationText = context.getString(R.string.starting_affirmation)
                        val packageName = context.packageName
                        val drawableResId = R.drawable.bg_purple_starry_lake
                        val imageUrl = "android.resource://$packageName/$drawableResId"
                        val currentTime = System.currentTimeMillis()

                        val sql = """
                    INSERT INTO affirmations (text, imageUrl, category, isFavorite, createdAt)
                    VALUES (?, ?, ?, ?, ?)
                """.trimIndent()

                        val bindArgs = arrayOf<Any>(
                            affirmationText,
                            imageUrl,
                            "self_love",
                            0,
                            currentTime
                        )

                        db.execSQL(sql, bindArgs)
                    }
                })
                .build()
        }

        @Provides
        @Singleton
        fun provideAffirmationDao(
            database: AppDatabase
        ): AffirmationDao = database.affirmationDao()
    }
}