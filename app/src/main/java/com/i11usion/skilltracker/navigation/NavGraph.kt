package com.i11usion.skilltracker.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.i11usion.skilltracker.presentation.screen.authentication.AuthScreen
import com.i11usion.skilltracker.presentation.screen.authentication.AuthViewModel
import com.i11usion.skilltracker.presentation.screen.home.HomeScreen

@Composable
fun NavGraph(
    viewModelFactory: ViewModelProvider.Factory
) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Screen.Auth.route
    ) {
        composable(Screen.Auth.route) { backStackEntry ->

            val viewModel: AuthViewModel = viewModel(
                viewModelStoreOwner = backStackEntry,
                factory = viewModelFactory
            )
            AuthScreen(viewModel = viewModel) {
                navController.navigate(Screen.Home.route) {
                    popUpTo(Screen.Auth.route) {
                        inclusive = true
                    }
                }
            }
        }

        composable(Screen.Home.route) {
            HomeScreen()
        }
    }
}

sealed class Screen(val route: String) {

    data object Auth : Screen("auth")
    data object Home : Screen("home")
}