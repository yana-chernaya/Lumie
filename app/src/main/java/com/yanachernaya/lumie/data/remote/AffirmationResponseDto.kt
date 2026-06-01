package com.yanachernaya.lumie.data.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AffirmationResponseDto(
    @SerialName("data")
    val data: AffirmationDataDto = AffirmationDataDto()
)
