package com.yanachernaya.lumie.data.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class BackgroundDataDto(
    @SerialName("imageUrl")
    val imageUrl: String = ""
)
