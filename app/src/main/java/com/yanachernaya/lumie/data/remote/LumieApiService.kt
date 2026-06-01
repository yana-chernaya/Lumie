package com.yanachernaya.lumie.data.remote

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface LumieApiService {

    @POST("api/affirmation")
    suspend fun generateAffirmation(
        @Body request: GenerateAffirmationRequest
    ): AffirmationResponseDto

    @GET("api/image")
    suspend fun getBackgroundImage(
        @Query("q") unsplashQuery: String
    ): BackgroundResponseDto
}