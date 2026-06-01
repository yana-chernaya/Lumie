package com.yanachernaya.lumie.data.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AffirmationDataDto(
    @SerialName("affirmation")
    val affirmation: String = "",
    @SerialName("unsplashQuery")
    val unsplashQuery: String = ""
)
