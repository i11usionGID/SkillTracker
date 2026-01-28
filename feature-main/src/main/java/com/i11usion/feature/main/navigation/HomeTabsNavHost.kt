package com.i11usion.feature.main.navigation

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.i11usion.core.navigation.HomeRoutes

@Composable
fun HomeTabsNavHost(
    navController: NavHostController
) {
    NavHost(
        navController = navController,
        startDestination = HomeRoutes.DASHBOARD
    ) {
        composable(HomeRoutes.DASHBOARD) { Text("DASHBOARD") }
        composable(HomeRoutes.TASKS) { Text("TASKS") }
        composable(HomeRoutes.USERS) { Text("USERS") }
        composable(HomeRoutes.TESTS) { Text("TESTS") }
    }
}
