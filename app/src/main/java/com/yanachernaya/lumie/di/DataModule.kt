package com.yanachernaya.lumie.di

import android.app.NotificationManager
import android.content.Context
import androidx.core.content.getSystemService
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.work.WorkManager
import com.yanachernaya.lumie.BuildConfig
import com.yanachernaya.lumie.R
import com.yanachernaya.lumie.data.background.WorkManagerBackgroundRefreshScheduler
import com.yanachernaya.lumie.data.local.database.AffirmationDao
import com.yanachernaya.lumie.data.local.database.AppDatabase
import com.yanachernaya.lumie.data.local.source.LocalContentDataSource
import com.yanachernaya.lumie.data.local.source.LocalContentDataSourceImpl
import com.yanachernaya.lumie.data.remote.LumieApiService
import com.yanachernaya.lumie.data.remote.interceptor.AuthInterceptor
import com.yanachernaya.lumie.data.repository.AffirmationRepositoryImpl
import com.yanachernaya.lumie.data.repository.ImageRepositoryImpl
import com.yanachernaya.lumie.data.repository.SettingsRepositoryImpl
import com.yanachernaya.lumie.domain.repository.AffirmationRepository
import com.yanachernaya.lumie.domain.repository.ImageRepository
import com.yanachernaya.lumie.domain.repository.SettingsRepository
import com.yanachernaya.lumie.domain.scheduler.BackgroundRefreshScheduler
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Converter
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import retrofit2.create
import java.util.concurrent.TimeUnit
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

    @Binds
    @Singleton
    fun bindLocalContentDataSource(
        impl: LocalContentDataSourceImpl
    ): LocalContentDataSource

    @Binds
    @Singleton
    fun bindBackgroundRefreshScheduler(
        impl: WorkManagerBackgroundRefreshScheduler
    ): BackgroundRefreshScheduler

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

        @Provides
        @Singleton
        fun provideJson(): Json {
            return Json {
                ignoreUnknownKeys = true
                coerceInputValues = true
            }
        }

        @Provides
        @Singleton
        fun provideConverterFactory(
            json: Json
        ): Converter.Factory {
            return json.asConverterFactory(
                "application/json".toMediaType()
            )
        }

        @Provides
        @Singleton
        fun provideOkHttpClient(): OkHttpClient {
            val builder = OkHttpClient.Builder()
                .addInterceptor(AuthInterceptor())
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(15, TimeUnit.SECONDS)

            if (BuildConfig.DEBUG) {
                builder.addInterceptor(HttpLoggingInterceptor().apply {
                    level = HttpLoggingInterceptor.Level.BODY
                })
            }
            return builder.build()
        }

        @Provides
        @Singleton
        fun provideRetrofit(
            converterFactory: Converter.Factory,
            okHttpClient: OkHttpClient
        ): Retrofit {
            return Retrofit.Builder()
                .baseUrl("https://affirmation.blvck.myds.me/")
                .client(okHttpClient)
                .addConverterFactory(converterFactory)
                .build()
        }

        @Provides
        @Singleton
        fun provideApiService(
            retrofit: Retrofit
        ): LumieApiService {
            return retrofit.create()
        }

        @Provides
        @Singleton
        fun provideWorkManager(
            @ApplicationContext context: Context
        ): WorkManager = WorkManager.getInstance(context)

        @Provides
        @Singleton
        fun provideNotificationManager(
            @ApplicationContext context: Context,
        ): NotificationManager =
            requireNotNull(context.getSystemService<NotificationManager>()) {
                "NotificationManager system service is unavailable"
            }
    }
}