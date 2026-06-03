package com.yanachernaya.lumie.presentation.ui.theme

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.view.Window
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.window.DialogWindowProvider
import androidx.core.view.WindowCompat

private tailrec fun Context.findWindow(): Window? =
    when (this) {
        is Activity -> window
        is ContextWrapper -> baseContext.findWindow()
        else -> null
    }

@Composable
fun SetStatusBarAppearance(isImageBackgroundScreen: Boolean) {
    val view = LocalView.current
    val isAppDarkTheme = LocalAppDarkTheme.current

    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.parent as? DialogWindowProvider)?.window
                ?: view.context.findWindow()

            if (window != null) {
                WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars =
                    if (isImageBackgroundScreen) {
                        false
                    } else {
                        !isAppDarkTheme
                    }
            }
        }
    }
}