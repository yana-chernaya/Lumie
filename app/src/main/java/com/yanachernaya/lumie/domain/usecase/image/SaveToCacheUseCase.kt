package com.yanachernaya.lumie.domain.usecase.image

import android.graphics.Bitmap
import android.net.Uri
import com.yanachernaya.lumie.domain.repository.ImageRepository
import javax.inject.Inject

class SaveToCacheUseCase @Inject constructor(
    private val imageRepository: ImageRepository
) {

    suspend operator fun invoke(bitmap: Bitmap): Uri? =
        imageRepository.saveToCache(bitmap)
}