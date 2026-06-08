package com.yanachernaya.lumie.presentation.utils

import android.graphics.Bitmap
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.layer.GraphicsLayer
import kotlinx.coroutines.launch
import kotlin.coroutines.cancellation.CancellationException

private const val TAG = "ImageCaptureUtils"

@Composable
fun rememberImageCapturer(
    graphicsLayer: GraphicsLayer
): ((ImageCaptureResult) -> Unit) -> Unit {

    val scope = rememberCoroutineScope()

    return remember(graphicsLayer, scope) {
        { onResult ->
            scope.launch {
                if (graphicsLayer.size.width <= 0 || graphicsLayer.size.height <= 0) {
                    onResult(ImageCaptureResult.ImageNotReady)
                    return@launch
                }

                try {
                    val bitmap = graphicsLayer.toImageBitmap().asAndroidBitmap()
                    onResult(ImageCaptureResult.Success(bitmap))
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    onResult(ImageCaptureResult.Error)
                    Log.e(TAG, "Image capture error", e)
                }
            }
        }
    }
}

sealed interface ImageCaptureResult {
    data class Success(val bitmap: Bitmap) : ImageCaptureResult
    data object ImageNotReady : ImageCaptureResult
    data object Error : ImageCaptureResult
}