package com.i11usion.feature.main.navigation

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.i11usion.core.navigation.HomeRoutes
import com.i11usion.feature.main.presentation.dashboard.DashboardScreen
import com.i11usion.feature.main.presentation.task.TaskScreen

@Composable
fun HomeTabsNavHost(
    navController: NavHostController
) {
    NavHost(
        navController = navController,
        startDestination = HomeRoutes.DASHBOARD
    ) {
        composable(HomeRoutes.DASHBOARD) {
            DashboardScreen(
                onCheckAllTaskClick = {},
                onTaskCardClick = {}
            )
        }
        composable(HomeRoutes.TASKS) {
            TaskScreen(
                onCreateTaskClick = {},
                onTaskCardClick = {}
            )
        }
        composable(HomeRoutes.USERS) { Text("USERS") }
        composable(HomeRoutes.TESTS) { Text("TESTS") }
    }
}
