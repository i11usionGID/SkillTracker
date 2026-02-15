package com.i11usion.feature.main.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.i11usion.core.navigation.HomeRoutes
import com.i11usion.feature.main.presentation.dashboard.DashboardScreen
import com.i11usion.feature.main.presentation.quiz.list.QuizScreen
import com.i11usion.feature.main.presentation.quiz.question.list.QuestionScreen
import com.i11usion.feature.main.presentation.task.create.CreateTaskScreen
import com.i11usion.feature.main.presentation.task.detail.TaskDetailScreen
import com.i11usion.feature.main.presentation.task.list.TaskScreen
import com.i11usion.feature.main.presentation.user.create.CreateUserScreen
import com.i11usion.feature.main.presentation.user.list.UserScreen

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
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                onTaskCardClick = { taskId ->
                    navController.navigate(HomeRoutes.taskDetail(taskId)) {
                        launchSingleTop = true
                    }
                }
            )
        }
        composable(HomeRoutes.TASKS) {
            TaskScreen(
                onCreateTaskClick = {
                    navController.navigate(HomeRoutes.CREATE_TASK) {
                        launchSingleTop = true
                    }
                },
                onTaskCardClick = { taskId ->
                    navController.navigate(HomeRoutes.taskDetail(taskId)) {
                        launchSingleTop = true
                    }
                }
            )
        }
        composable(HomeRoutes.USERS) {
            UserScreen(
                onCreateUserClick = {
                    navController.navigate(HomeRoutes.CREATE_USER) {
                        launchSingleTop = true
                    }
                }
            )
        }
        composable(HomeRoutes.TESTS) {
            QuizScreen(
                onCreateTestClick = {},
                onOpenQuestionListClick = {
                    navController.navigate(HomeRoutes.QUESTIONS) {
                        launchSingleTop = true
                    }
                },
                onOpenQuizClick = { TODO() },
                onStartTestingClick = { TODO() }
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
        composable(HomeRoutes.CREATE_TASK) {
            CreateTaskScreen(
                onCancelClick = { navController.popBackStack() },
                onCreateTaskClick = { navController.popBackStack() }
            )
        }
        composable(HomeRoutes.CREATE_USER) {
            CreateUserScreen(
                onBack = { navController.popBackStack() }
            )
        }
        composable(HomeRoutes.QUESTIONS) {
            QuestionScreen(
                onBackClick = { navController.popBackStack() },
                onCreateQuestionClick = { TODO() },
                onUpdateQuestionClick = { TODO() }
            )
        }
    }
}
