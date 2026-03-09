package com.i11usion.feature.main.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.i11usion.core.navigation.HomeRoutes
import com.i11usion.core.ui.di.LocalViewModelFactory
import com.i11usion.core.ui.helpers.asString
import com.i11usion.core.ui.snackbar.LocalAppSnackbarHostState
import com.i11usion.core.ui.template.ErrorScreen
import com.i11usion.feature.main.navigation.HomeTabsNavHost
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce

@OptIn(FlowPreview::class)
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

    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current


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
        viewModel.events.collectLatest { event ->
            when (event) {
                HomeEvent.Logout -> onLogoutNavigate()
            }
        }
    }

    LaunchedEffect(Unit) {
        viewModel.snackbarManager.events
            .debounce(100)
            .collect { message ->
                snackbarHostState.showSnackbar(
                    message = message.asString(context),
                    duration = SnackbarDuration.Short
                )
            }
    }

    CompositionLocalProvider(
        LocalAppSnackbarHostState provides snackbarHostState
    ) {
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
                val fsError = s.fullScreenError
                if (fsError != null) {
                    ErrorScreen(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.surface),
                        iconResId = fsError.iconResId,
                        cdResId = fsError.cdResId,
                        errorText = fsError.text.asString(context),
                        onRefreshClick = viewModel::onErrorRefreshClick
                    )
                    return@CompositionLocalProvider
                }

                val contentState = s.copy(selectedScreenRoute = selected)

                HomeShell(
                    isDarkTheme = isDarkTheme,
                    state = contentState,
                    onDrawerItemClick = { route ->
                        while (homeNavController.currentBackStackEntry?.destination?.route !in HomeRoutes.TOP_LEVEL) {
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
                    content = { HomeTabsNavHost(navController = homeNavController) }
                )
            }

            is HomeState.Error -> {
                ErrorScreen(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.surface),
                    iconResId = s.model.iconResId,
                    cdResId = s.model.cdResId,
                    errorText = s.model.text.asString(context),
                    onRefreshClick = viewModel::onErrorRefreshClick
                )
            }
        }
    }
}

