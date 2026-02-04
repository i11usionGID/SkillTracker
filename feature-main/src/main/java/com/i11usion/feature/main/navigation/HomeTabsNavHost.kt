package com.i11usion.feature.main.navigation

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.i11usion.core.navigation.HomeRoutes
import com.i11usion.feature.main.presentation.dashboard.DashboardScreen
import com.i11usion.feature.main.presentation.task.TaskScreen
import com.i11usion.feature.main.presentation.user.UserScreen

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
        composable(HomeRoutes.USERS) {
            UserScreen(
                onCreateUserClick = {}
            )
        }
        composable(HomeRoutes.TESTS) { Text("TESTS") }
    }
}
