package com.yanachernaya.lumie.data.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class BackgroundResponseDto(
    @SerialName("data")
    val data: BackgroundDataDto = BackgroundDataDto()
)
