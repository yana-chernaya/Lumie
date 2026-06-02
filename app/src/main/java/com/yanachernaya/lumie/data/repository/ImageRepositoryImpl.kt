package com.yanachernaya.lumie.data.repository

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import androidx.core.content.FileProvider
import com.yanachernaya.lumie.domain.repository.ImageRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

private const val TAG = "ImageRepositoryImpl"

class ImageRepositoryImpl @Inject constructor(
    @param:ApplicationContext private val context: Context
) : ImageRepository {

    override suspend fun saveToGallery(bitmap: Bitmap): Boolean {
        return withContext(Dispatchers.IO) {
            var uri: Uri? = null
            val resolver = context.contentResolver

            try {
                val fileName = "Affirmation_${System.currentTimeMillis()}.jpeg"

                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                    put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        put(
                            MediaStore.MediaColumns.RELATIVE_PATH,
                            Environment.DIRECTORY_PICTURES + "/Affirmations"
                        )
                        put(MediaStore.MediaColumns.IS_PENDING, 1)
                    }
                }

                uri = resolver.insert(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    contentValues
                ) ?: return@withContext false

                val outputStream = resolver.openOutputStream(uri)
                if (outputStream == null) {
                    resolver.delete(uri, null, null)
                    return@withContext false
                }

                outputStream.use {
                    val isSuccess = bitmap.compress(
                        Bitmap.CompressFormat.JPEG, 95, it
                    )
                    if (!isSuccess) {
                        resolver.delete(uri, null, null)
                        return@withContext false
                    }
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    contentValues.clear()
                    contentValues.put(MediaStore.MediaColumns.IS_PENDING, 0)
                    resolver.update(uri, contentValues, null, null)
                }

                return@withContext true

            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "Error saving to gallery", e)

                uri?.let { uri ->
                    try {
                        resolver.delete(uri, null, null)
                    } catch (ignore: Exception) {
                    }
                }
                return@withContext false
            }
        }
    }

    override suspend fun saveToCache(bitmap: Bitmap): Uri? {
        return withContext(Dispatchers.IO) {
            try {
                val cacheDir = File(context.cacheDir, "shared_images").apply {
                    if (!exists()) mkdirs()
                }
                val file = File(cacheDir, "shared_affirmation_${System.currentTimeMillis()}.png")

                file.outputStream().use { outputStream ->
                    val isSuccess = bitmap.compress(
                        Bitmap.CompressFormat.PNG,
                        100,
                        outputStream
                    )

                    if (!isSuccess) {
                        file.delete()
                        return@withContext null
                    }
                }

                FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.provider",
                    file
                )
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "Error saving to cache", e)
                null
            }
        }
    }
}