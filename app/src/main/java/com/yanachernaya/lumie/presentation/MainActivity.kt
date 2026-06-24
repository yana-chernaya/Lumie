package com.yanachernaya.lumie.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.getValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.yanachernaya.lumie.domain.entity.AppTheme
import com.yanachernaya.lumie.presentation.navigation.NavGraph
import com.yanachernaya.lumie.presentation.screens.home.HomeState
import com.yanachernaya.lumie.presentation.screens.home.HomeViewModel
import com.yanachernaya.lumie.presentation.ui.theme.LumieTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val mainViewModel: MainViewModel by viewModels()
    private val homeViewModel: HomeViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        splashScreen.setKeepOnScreenCondition {
            homeViewModel.state.value is HomeState.Initial
        }

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
                NavGraph(homeViewModel = homeViewModel)
            }
        }
    }
}
