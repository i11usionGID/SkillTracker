package com.i11usion.feature.main.presentation

import androidx.compose.foundation.background
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
    isDarkTheme: Boolean,
    onLogoutNavigate: () -> Unit,
    onSwitchThemeClick: () -> Unit
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

        HomeRoutes.USERS,
        HomeRoutes.CREATE_USER -> HomeRoutes.USERS

        HomeRoutes.TESTS,
        HomeRoutes.CREATE_QUIZ,
        HomeRoutes.UPDATE_QUIZ,
        HomeRoutes.QUIZ_DETAIL,
        HomeRoutes.CREATE_QUESTION_SECTION,
        HomeRoutes.UPDATE_QUESTION_SECTION,
        HomeRoutes.CREATE_ASSIGNMENT,
        HomeRoutes.QUESTIONS,
        HomeRoutes.CREATE_QUESTION,
        HomeRoutes.UPDATE_QUESTION,
        HomeRoutes.TESTING,
        HomeRoutes.CHECK_ATTEMPTS,
        HomeRoutes.ATTEMPT_ANALYTIC_DETAIL -> HomeRoutes.TESTS

        else -> null
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
                    .fillMaxSize()
                    .background(color = MaterialTheme.colorScheme.surface),
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
                isDarkTheme = isDarkTheme,
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
                onNotificationClick = {
                    homeNavController.navigate(HomeRoutes.NOTIFICATIONS) {
                        launchSingleTop = true
                    }
                },
                onSwitchThemeClick = onSwitchThemeClick,
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

