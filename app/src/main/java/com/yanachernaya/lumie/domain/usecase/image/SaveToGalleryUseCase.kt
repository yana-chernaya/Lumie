package com.yanachernaya.lumie.domain.usecase.image

import android.graphics.Bitmap
import com.yanachernaya.lumie.domain.repository.ImageRepository
import javax.inject.Inject

class SaveToGalleryUseCase @Inject constructor(
    private val imageRepository: ImageRepository
) {

    suspend operator fun invoke(bitmap: Bitmap): Boolean =
        imageRepository.saveToGallery(bitmap)
}