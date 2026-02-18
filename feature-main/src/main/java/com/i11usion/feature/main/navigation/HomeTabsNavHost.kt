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
import com.i11usion.feature.main.presentation.quiz.create.CreateQuizScreen
import com.i11usion.feature.main.presentation.quiz.detail.QuizDetailScreen
import com.i11usion.feature.main.presentation.quiz.detail.assignment_section.CreateAssignmentScreen
import com.i11usion.feature.main.presentation.quiz.detail.question_section.create.CreateQuestionSectionScreen
import com.i11usion.feature.main.presentation.quiz.list.QuizScreen
import com.i11usion.feature.main.presentation.quiz.question.create.CreateQuestionScreen
import com.i11usion.feature.main.presentation.quiz.question.list.QuestionScreen
import com.i11usion.feature.main.presentation.quiz.question.update.UpdateQuestionScreen
import com.i11usion.feature.main.presentation.quiz.testing.TestScreen
import com.i11usion.feature.main.presentation.quiz.update.UpdateQuizScreen
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
                onCreateTestClick = {
                    navController.navigate(HomeRoutes.CREATE_QUIZ) {
                        launchSingleTop = true
                    }
                },
                onOpenQuestionListClick = {
                    navController.navigate(HomeRoutes.QUESTIONS) {
                        launchSingleTop = true
                    }
                },
                onOpenQuizClick = { quizId ->
                    navController.navigate(HomeRoutes.quizDetail(quizId)) {
                        launchSingleTop = true
                    }
                },
                onStartTestingClick = { quizId ->
                    navController.navigate(HomeRoutes.quizTesting(quizId)) {
                        launchSingleTop = true
                    }
                }
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
                onCreateQuestionClick = {
                    navController.navigate(HomeRoutes.CREATE_QUESTION) {
                        launchSingleTop = true
                    }
                },
                onUpdateQuestionClick = { questionId ->
                    navController.navigate(HomeRoutes.updateQuestion(questionId)) {
                        launchSingleTop = true
                    }
                }
            )
        }
        composable(HomeRoutes.CREATE_QUESTION) {
            CreateQuestionScreen(
                onCancelClick = { navController.popBackStack() }
            )
        }
        composable(
            route = HomeRoutes.UPDATE_QUESTION,
            arguments = listOf(navArgument("questionId") { type = NavType.IntType })
        ) { entry ->
            val questionId = entry.arguments?.getInt("questionId") ?: return@composable

            UpdateQuestionScreen(
                questionId = questionId,
                onCancelClick = { navController.popBackStack() }
            )
        }
        composable(HomeRoutes.CREATE_QUIZ) {
            CreateQuizScreen(
                onCancelClick = { navController.popBackStack() }
            )
        }
        composable(
            route = HomeRoutes.QUIZ_DETAIL,
            arguments = listOf(navArgument("quizId") { type = NavType.IntType })
        ) { entry ->
            val quizId = entry.arguments?.getInt("quizId") ?: return@composable

            QuizDetailScreen(
                quizId = quizId,
                onBack = { navController.popBackStack() },
                onCreateSectionClick = { questionId ->
                    navController.navigate(HomeRoutes.createQuestionSection(questionId)) {
                        launchSingleTop = true
                    }
                },
                onUpdateQuizClick = { questionId ->
                    navController.navigate(HomeRoutes.updateQuiz(questionId)) {
                        launchSingleTop = true
                    }
                },
                onCreateAssignmentClick = { questionId ->
                    navController.navigate(HomeRoutes.createAssignment(questionId)) {
                        launchSingleTop = true
                    }
                },
                onEditQuestionSectionClick = { TODO() },
                onCheckAttemptsClick = { TODO() }
            )
        }
        composable(
            route = HomeRoutes.TESTING,
            arguments = listOf(navArgument("quizId") { type = NavType.IntType })
        ) { entry ->
            val quizId = entry.arguments?.getInt("quizId") ?: return@composable

            TestScreen(
                quizId = quizId,
                onNavigateToAnalytics = { attempt ->
                    TODO()
                },
                onBack = { navController.popBackStack() },
            )
        }
        composable(
            route = HomeRoutes.CREATE_QUESTION_SECTION,
            arguments = listOf(navArgument("quizId") { type = NavType.IntType })
        ) { entry ->
            val quizId = entry.arguments?.getInt("quizId") ?: return@composable

            CreateQuestionSectionScreen(
                quizId = quizId,
                onCreateQuestionsClick = {
                    navController.popBackStack()
                    navController.navigate(HomeRoutes.QUESTIONS) {
                        launchSingleTop = true
                    }
                },
                onCancelClick = { navController.popBackStack() }
            )
        }
        composable(
            route = HomeRoutes.CREATE_ASSIGNMENT,
            arguments = listOf(navArgument("quizId") { type = NavType.IntType })
        ) { entry ->
            val quizId = entry.arguments?.getInt("quizId") ?: return@composable

            CreateAssignmentScreen(
                quizId = quizId,
                onCancelClick = { navController.popBackStack() }
            )
        }
        composable(
            route = HomeRoutes.UPDATE_QUIZ,
            arguments = listOf(navArgument("quizId") { type = NavType.IntType })
        ) { entry ->
            val quizId = entry.arguments?.getInt("quizId") ?: return@composable

            UpdateQuizScreen(
                quizId = quizId,
                onCancelClick = { navController.popBackStack() }
            )
        }
    }
}
