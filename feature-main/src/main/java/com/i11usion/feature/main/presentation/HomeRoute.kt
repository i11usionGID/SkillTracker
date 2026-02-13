package com.i11usion.feature.main.presentation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.i11usion.core.navigation.HomeRoutes
import com.i11usion.core.navigation.HomeRoutes.TOP_LEVEL
import com.i11usion.core.ui.di.LocalViewModelFactory
import com.i11usion.feature.main.navigation.HomeTabsNavHost

@Composable
fun HomeRoute(
    onLogoutNavigate: () -> Unit
) {
    val viewModel: HomeViewModel = viewModel(factory = LocalViewModelFactory.current)
    val state by viewModel.state.collectAsState()

    val homeNavController = rememberNavController()
    val backStackEntry by homeNavController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    val selected = when (currentRoute) {
        HomeRoutes.DASHBOARD -> HomeRoutes.DASHBOARD
        HomeRoutes.TASKS,
        HomeRoutes.TASK_DETAIL_ROUTE,
        HomeRoutes.CREATE_TASK -> HomeRoutes.TASKS

        HomeRoutes.USERS -> HomeRoutes.USERS
        HomeRoutes.TESTS -> HomeRoutes.TESTS

        else -> HomeRoutes.DASHBOARD
    }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            if (event is HomeEvent.Logout) onLogoutNavigate()
        }
    }

    when (val s = state) {
        HomeState.Initial -> {}
        HomeState.Loading -> {
            Box(
                modifier = Modifier
                    .fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        is HomeState.Content -> {
            val contentState = s.copy(selectedScreenRoute = selected)
            HomeShell(
                state = contentState,
                onDrawerItemClick = { route ->

                    while (homeNavController.currentBackStackEntry?.destination?.route !in TOP_LEVEL) {
                        val popped = homeNavController.popBackStack()
                        if (!popped) break
                    }

                    homeNavController.navigate(route) {
                        popUpTo(homeNavController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                onLogoutClick = viewModel::onLogoutClick,
                content = {
                    HomeTabsNavHost(navController = homeNavController)
                }
            )
        }

        is HomeState.Error -> {
            Text(text = s.message)
        }
    }
}

