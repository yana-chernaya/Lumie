package com.yanachernaya.lumie.data.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GenerateAffirmationRequest(
    @SerialName("topic")
    val category: String,
    @SerialName("user_gender")
    val userGender: String
)
