package com.yanachernaya.lumie.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.yanachernaya.lumie.presentation.screens.details.AffirmationDetailsScreen
import com.yanachernaya.lumie.presentation.screens.favorites.FavoritesScreen
import com.yanachernaya.lumie.presentation.screens.home.HomeScreen
import com.yanachernaya.lumie.presentation.screens.home.HomeViewModel
import com.yanachernaya.lumie.presentation.screens.settings.SettingsScreen

@Composable
fun NavGraph(homeViewModel: HomeViewModel) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Screen.Home.route
    ) {
        composable(Screen.Home.route) {
            HomeScreen(
                viewModel = homeViewModel,
                onNavigateToFavorites = { navController.navigateBottomTab(Screen.Favorites.route) },
                onNavigateToSettings = { navController.navigate(Screen.Settings.route) }
            )
        }

        composable(Screen.Favorites.route) {
            FavoritesScreen(
                onNavigateToHome = { navController.navigateBottomTab(Screen.Home.route) },
                onNavigateToSettings = { navController.navigate(Screen.Settings.route) },
                onNavigateToDetails = { id ->
                    navController.navigate(Screen.AffirmationDetails.getRoute(id))
                }
            )
        }

        composable(
            route = Screen.AffirmationDetails.route,
            arguments = listOf(
                navArgument("id") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            AffirmationDetailsScreen(
                id = backStackEntry.arguments?.getInt("id") ?: 0,
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(Screen.Settings.route) {
            SettingsScreen(
                onBackClick = { navController.popBackStack() }
            )
        }
    }
}

sealed class Screen(val route: String) {
    data object Home : Screen("home")
    data object Favorites : Screen("favorites")
    data object AffirmationDetails : Screen("affirmation_details/{id}") {
        fun getRoute(id: Int): String {
            return "affirmation_details/$id"
        }
    }

    data object Settings : Screen("settings")
}

fun NavController.navigateBottomTab(route: String) {
    navigate(route) {
        popUpTo(graph.findStartDestination().id) {
            saveState = true
        }
        launchSingleTop = true
        restoreState = true
    }
}

