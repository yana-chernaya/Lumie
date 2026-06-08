package com.yanachernaya.lumie.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.yanachernaya.lumie.domain.entity.AppTheme
import com.yanachernaya.lumie.presentation.screens.home.HomeScreen
import com.yanachernaya.lumie.presentation.ui.theme.LumieTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val mainViewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {

            val theme by mainViewModel.appThemeState.collectAsStateWithLifecycle()
            val isDarkTheme = when (theme) {
                AppTheme.LIGHT -> false
                AppTheme.DARK -> true
                AppTheme.SYSTEM -> isSystemInDarkTheme()
            }

            LumieTheme(
                darkTheme = isDarkTheme
            ) {
                HomeScreen(
                    onNavigateToSettings = {},
                    onNavigateToFavorites = {}
                )
            }
        }
    }
}
