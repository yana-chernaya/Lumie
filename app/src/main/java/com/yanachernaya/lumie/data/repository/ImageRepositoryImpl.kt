package com.yanachernaya.lumie.data.repository

import android.graphics.Bitmap
import android.net.Uri
import com.yanachernaya.lumie.domain.repository.ImageRepository
import javax.inject.Inject

class ImageRepositoryImpl @Inject constructor() : ImageRepository {

    override suspend fun saveToGallery(bitmap: Bitmap): Boolean {
        TODO("Not yet implemented")
    }

    override suspend fun saveToCache(bitmap: Bitmap): Uri? {
        TODO("Not yet implemented")
    }
}