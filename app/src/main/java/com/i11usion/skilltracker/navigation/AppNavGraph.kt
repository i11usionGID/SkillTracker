package com.i11usion.skilltracker.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable

@Composable
fun AppNavGraph(
    navigationState: NavigationState,
    authScreen: @Composable (onAuthSuccess: () -> Unit) -> Unit,
    dashboardScreen: @Composable () -> Unit,
    tasksScreen: @Composable () -> Unit,
    usersScreen: @Composable () -> Unit,
    testsScreen: @Composable () -> Unit
) {
    NavHost(
        navController = navigationState.navController,
        startDestination = Screen.Auth.route
    ) {

        composable(Screen.Auth.route) {
            authScreen {
                navigationState.navigateAndClearBackStack(
                    Screen.Home.Dashboard.route
                )
            }
        }

        composable(Screen.Home.Dashboard.route) { dashboardScreen() }
        composable(Screen.Home.Tasks.route) { tasksScreen() }
        composable(Screen.Home.Users.route) { usersScreen() }
        composable(Screen.Home.Tests.route) { testsScreen() }
    }
}

