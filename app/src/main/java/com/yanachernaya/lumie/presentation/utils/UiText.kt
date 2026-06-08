package com.yanachernaya.lumie.presentation.utils

import android.content.Context
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource

sealed class UiText {
    object Empty : UiText()
    class StringResource(
        @param:StringRes val resId: Int,
        vararg val arg: Any
    ) : UiText()

    @Composable
    fun asString(): String {
        return when(this) {
            is Empty -> ""
            is StringResource -> stringResource(resId, *arg)
        }
    }

    fun asString(context: Context): String {
        return when(this) {
            is Empty -> ""
            is StringResource -> context.getString(resId, *arg)
        }
    }
}