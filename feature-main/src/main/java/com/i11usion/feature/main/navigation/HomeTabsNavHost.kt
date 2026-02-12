package com.i11usion.feature.main.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.i11usion.core.navigation.HomeRoutes
import com.i11usion.feature.main.presentation.dashboard.DashboardScreen
import com.i11usion.feature.main.presentation.quiz.QuizScreen
import com.i11usion.feature.main.presentation.task.detail.TaskDetailScreen
import com.i11usion.feature.main.presentation.task.list.TaskScreen
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
                onCheckAllTaskClick = {
                    navController.navigate(HomeRoutes.TASKS) {
                        popUpTo(HomeRoutes.DASHBOARD) {
                            inclusive = false
                        }
                    }
                },
                onTaskCardClick = {
                        taskId ->
                    navController.navigate(HomeRoutes.taskDetail(taskId)) {
                        launchSingleTop = true
                    }
                }
            )
        }
        composable(HomeRoutes.TASKS) {
            TaskScreen(
                onCreateTaskClick = {},
                onTaskCardClick = { taskId ->
                    navController.navigate(HomeRoutes.taskDetail(taskId)) {
                        launchSingleTop = true
                    }
                }
            )
        }
        composable(HomeRoutes.USERS) {
            UserScreen(
                onCreateUserClick = {}
            )
        }
        composable(HomeRoutes.TESTS) {
            QuizScreen(
                onCreateTestClick = {},
                onOpenQuestionListClick = {},
                onOpenQuizClick = {},
                onStartTestingClick = {}
            )
        }
        composable(
            route = HomeRoutes.TASK_DETAIL_ROUTE,
            arguments = listOf(navArgument("taskId") { type = NavType.IntType })
        ) { entry ->
            val taskId = entry.arguments?.getInt("taskId") ?: return@composable

            TaskDetailScreen(
                taskId = taskId,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
