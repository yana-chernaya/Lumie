package com.yanachernaya.lumie.presentation.utils

import android.content.ActivityNotFoundException
import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import com.yanachernaya.lumie.R

private const val TAG = "ShareUtils"
private const val IMAGE_MIME_TYPE = "image/png"

fun shareImage(
    context: Context,
    uri: Uri,
    onFinish: () -> Unit
): ShareResult {
    val sendIntent = Intent(Intent.ACTION_SEND).apply {
        type = IMAGE_MIME_TYPE
        putExtra(Intent.EXTRA_STREAM, uri)
        clipData = ClipData.newRawUri("", uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    val chooserIntent = Intent.createChooser(
        sendIntent,
        context.getString(R.string.title_share_affirmation)
    ).apply {
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }

    return try {
        context.startActivity(chooserIntent)
        ShareResult.Success
    } catch (e: ActivityNotFoundException) {
        Log.e(TAG, "No app found to share", e)
        ShareResult.NoAppFound
    } catch (e: Exception) {
        Log.e(TAG, "Error during sharing process", e)
        ShareResult.Error
    } finally {
        onFinish()
    }
}

sealed interface ShareResult {
    data object Success : ShareResult
    data object NoAppFound : ShareResult
    data object Error : ShareResult
}