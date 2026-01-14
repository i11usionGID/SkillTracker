package com.i11usion.skilltracker.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController

class NavigationState(
    val navController: NavHostController
) {

    fun navigateTo(route: String) {
        navController.navigate(route) {
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }

    fun navigateAndClearBackStack(route: String) {
        navController.navigate(route) {
            popUpTo(0)
        }
    }
}

@Composable
fun rememberNavigationState(
    navController: NavHostController = rememberNavController()
): NavigationState = remember {
    NavigationState(navController)
}

