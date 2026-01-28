package com.i11usion.skilltracker.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.i11usion.core.navigation.RootRoute

@Composable
fun AppNavGraph(
    navigationState: NavigationState,
    authScreen: @Composable (onAuthSuccess: () -> Unit) -> Unit,
    homeScreen: @Composable (onLogout: () -> Unit) -> Unit
) {
    NavHost(
        navController = navigationState.navController,
        startDestination = RootRoute.AUTH
    ) {

        composable(RootRoute.AUTH) {
            authScreen {
                navigationState.navigateAndClearBackStack(RootRoute.HOME)
            }
        }

        composable(RootRoute.HOME) {
            homeScreen {
                navigationState.navigateAndClearBackStack(RootRoute.AUTH)
            }
        }
    }
}

