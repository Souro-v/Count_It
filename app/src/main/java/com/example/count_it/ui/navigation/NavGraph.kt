package com.example.count_it.ui.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.count_it.ui.screens.CameraScreen
import com.example.count_it.ui.screens.HistoryScreen
import com.example.count_it.ui.screens.HomeScreen
import com.example.count_it.ui.screens.ResultScreen
import com.example.count_it.viewmodel.FoodViewModel

// Screen routes
sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Camera : Screen("camera")
    object Result : Screen("result")
    object History : Screen("history")
}

@Composable
fun NavGraph(navController: NavHostController) {
    val viewModel: FoodViewModel = viewModel()

    NavHost(
        navController = navController,
        startDestination = Screen.Home.route
    ) {
        composable(Screen.Home.route) {
            HomeScreen(
                viewModel = viewModel,
                onNavigateToCamera = {
                    navController.navigate(Screen.Camera.route)
                },
                onNavigateToHistory = {
                    navController.navigate(Screen.History.route)
                }
            )
        }

        composable(Screen.Camera.route) {
            CameraScreen(
                viewModel = viewModel,
                onNavigateToResult = {
                    navController.navigate(Screen.Result.route)
                },
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.Result.route) {
            ResultScreen(
                viewModel = viewModel,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateHome = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.History.route) {
            HistoryScreen(
                viewModel = viewModel,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}