package com.i11usion.skilltracker.navigation

import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.i11usion.core.domain.repository.SessionManager
import com.i11usion.core.navigation.RootRoute

@Composable
fun AppNavGraph(
    navigationState: NavigationState,
    sessionManager: SessionManager,
    authScreen: @Composable (onAuthSuccess: () -> Unit) -> Unit,
    homeScreen: @Composable (onLogout: () -> Unit) -> Unit
) {
    val navController = navigationState.navController
    val context = LocalContext.current

    LaunchedEffect(sessionManager) {
        sessionManager.sessionExpiredFlow.collect {
            val isOnAuth =
                navController.currentDestination?.route?.startsWith(RootRoute.AUTH) == true
            if (!isOnAuth) {
                navigationState.navigateAndClearBackStack(RootRoute.AUTH)
            }

            Toast
                .makeText(
                    context,
                    "Сессия истекла. Авторизуйтесь снова.",
                    Toast.LENGTH_LONG
                )
                .show()
        }
    }

    NavHost(
        navController = navController,
        startDestination = RootRoute.AUTH
    ) {

        composable(
            route = "${RootRoute.AUTH}?expired={expired}",
            arguments = listOf(
                navArgument("expired") {
                    type = NavType.BoolType
                    defaultValue = false
                }
            )
        ) { backStackEntry ->
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

