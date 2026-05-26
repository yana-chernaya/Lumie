package com.yanachernaya.lumie.domain.repository

import android.graphics.Bitmap
import android.net.Uri

interface ImageRepository {

    suspend fun saveToGallery(bitmap: Bitmap): Boolean

    suspend fun saveToCache(bitmap: Bitmap): Uri?
}