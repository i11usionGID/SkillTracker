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
import com.i11usion.feature.main.presentation.notification.NotificationScreen
import com.i11usion.feature.main.presentation.quiz.create.CreateQuizScreen
import com.i11usion.feature.main.presentation.quiz.detail.QuizDetailScreen
import com.i11usion.feature.main.presentation.quiz.detail.assignment_section.CreateAssignmentScreen
import com.i11usion.feature.main.presentation.quiz.detail.question_section.create.CreateQuestionSectionScreen
import com.i11usion.feature.main.presentation.quiz.detail.question_section.update.UpdateQuestionSectionScreen
import com.i11usion.feature.main.presentation.quiz.generate.GenerateQuizScreen
import com.i11usion.feature.main.presentation.quiz.list.QuizScreen
import com.i11usion.feature.main.presentation.quiz.question.create.CreateQuestionScreen
import com.i11usion.feature.main.presentation.quiz.question.list.QuestionScreen
import com.i11usion.feature.main.presentation.quiz.question.update.UpdateQuestionScreen
import com.i11usion.feature.main.presentation.quiz.testing.analytics.detail.AttemptAnalyticsDetailScreen
import com.i11usion.feature.main.presentation.quiz.testing.analytics.list.AttemptsAnalyticsScreen
import com.i11usion.feature.main.presentation.quiz.testing.test.TestScreen
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
                },
                onBackClick = { navController.safePopBackStack() }
            )
        }
        composable(HomeRoutes.NOTIFICATIONS) {
            NotificationScreen(navController)
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
                onGenerateQuizClick = {
                    navController.navigate(HomeRoutes.GENERATE_QUIZ) {
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
                onBack = { navController.safePopBackStack() }
            )
        }
        composable(HomeRoutes.CREATE_TASK) {
            CreateTaskScreen(
                onCancelClick = { navController.safePopBackStack() }
            )
        }
        composable(HomeRoutes.CREATE_USER) {
            CreateUserScreen(
                onBack = { navController.safePopBackStack() }
            )
        }
        composable(HomeRoutes.QUESTIONS) {
            QuestionScreen(
                onBackClick = { navController.safePopBackStack() },
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
                onCancelClick = { navController.safePopBackStack() }
            )
        }
        composable(
            route = HomeRoutes.UPDATE_QUESTION,
            arguments = listOf(navArgument("questionId") { type = NavType.IntType })
        ) { entry ->
            val questionId = entry.arguments?.getInt("questionId") ?: return@composable

            UpdateQuestionScreen(
                questionId = questionId,
                onCancelClick = { navController.safePopBackStack() }
            )
        }
        composable(HomeRoutes.CREATE_QUIZ) {
            CreateQuizScreen(
                onCancelClick = { navController.safePopBackStack() }
            )
        }
        composable(HomeRoutes.GENERATE_QUIZ) {
            GenerateQuizScreen(
                onCancelClick = { navController.safePopBackStack() }
            )
        }
        composable(
            route = HomeRoutes.QUIZ_DETAIL,
            arguments = listOf(navArgument("quizId") { type = NavType.IntType })
        ) { entry ->
            val quizId = entry.arguments?.getInt("quizId") ?: return@composable

            QuizDetailScreen(
                quizId = quizId,
                onBack = { navController.safePopBackStack() },
                onCreateSectionClick = { quizId ->
                    navController.navigate(HomeRoutes.createQuestionSection(quizId)) {
                        launchSingleTop = true
                    }
                },
                onUpdateQuizClick = { quizId ->
                    navController.navigate(HomeRoutes.updateQuiz(quizId)) {
                        launchSingleTop = true
                    }
                },
                onCreateAssignmentClick = { quizId ->
                    navController.navigate(HomeRoutes.createAssignment(quizId)) {
                        launchSingleTop = true
                    }
                },
                onEditQuestionSectionClick = { sectionId ->
                    navController.navigate(
                        HomeRoutes.updateQuestionSection(
                            quizId = quizId,
                            sectionId = sectionId
                        )
                    ) {
                        launchSingleTop = true
                    }
                },
                onCheckAttemptsClick = { quizId ->
                    navController.navigate(HomeRoutes.checkAttempts(quizId)) {
                        launchSingleTop = true
                    }
                },
                onCheckAllAttemptsClick = { quizId ->
                    navController.navigate(HomeRoutes.checkAttempts(quizId)) {
                        launchSingleTop = true
                    }
                }
            )
        }
        composable(
            route = HomeRoutes.TESTING,
            arguments = listOf(navArgument("quizId") { type = NavType.IntType })
        ) { entry ->
            val quizId = entry.arguments?.getInt("quizId") ?: return@composable

            TestScreen(
                quizId = quizId,
                onNavigateToAnalytics = { attemptId ->
                    navController.navigate(HomeRoutes.attemptAnalyticDetail(attemptId)) {
                        launchSingleTop = true
                        popUpTo(HomeRoutes.TESTING) { inclusive = true }
                    }
                },
                onBack = { navController.safePopBackStack() },
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
                    navController.safePopBackStack()
                    navController.navigate(HomeRoutes.QUESTIONS) {
                        launchSingleTop = true
                    }
                },
                onCancelClick = { navController.safePopBackStack() }
            )
        }
        composable(
            route = HomeRoutes.UPDATE_QUESTION_SECTION,
            arguments = listOf(
                navArgument("quizId") { type = NavType.IntType },
                navArgument("sectionId") { type = NavType.IntType }
            )
        ) { entry ->
            val quizId = entry.arguments?.getInt("quizId") ?: return@composable
            val sectionId = entry.arguments?.getInt("sectionId") ?: return@composable

            UpdateQuestionSectionScreen(
                quizId = quizId,
                sectionId = sectionId,
                onCreateQuestionsClick = {
                    navController.navigate(HomeRoutes.QUESTIONS) {
                        launchSingleTop = true
                    }
                },
                onCancelClick = {
                    navController.safePopBackStack()
                }
            )
        }
        composable(
            route = HomeRoutes.CREATE_ASSIGNMENT,
            arguments = listOf(navArgument("quizId") { type = NavType.IntType })
        ) { entry ->
            val quizId = entry.arguments?.getInt("quizId") ?: return@composable

            CreateAssignmentScreen(
                quizId = quizId,
                onCancelClick = { navController.safePopBackStack() }
            )
        }
        composable(
            route = HomeRoutes.UPDATE_QUIZ,
            arguments = listOf(navArgument("quizId") { type = NavType.IntType })
        ) { entry ->
            val quizId = entry.arguments?.getInt("quizId") ?: return@composable

            UpdateQuizScreen(
                quizId = quizId,
                onCancelClick = { navController.safePopBackStack() }
            )
        }
        composable(
            route = HomeRoutes.CHECK_ATTEMPTS,
            arguments = listOf(navArgument("quizId") { type = NavType.IntType })
        ) { entry ->
            val quizId = entry.arguments?.getInt("quizId") ?: return@composable

            AttemptsAnalyticsScreen(
                quizId = quizId,
                onBack = { navController.safePopBackStack() },
                onAttemptDetailScreen = { attemptId ->
                    navController.navigate(HomeRoutes.attemptAnalyticDetail(attemptId)) {
                        launchSingleTop = true
                    }
                }
            )
        }
        composable(
            route = HomeRoutes.ATTEMPT_ANALYTIC_DETAIL,
            arguments = listOf(navArgument("attemptId") { type = NavType.IntType })
        ) { entry ->
            val attemptId = entry.arguments?.getInt("attemptId") ?: return@composable

            AttemptAnalyticsDetailScreen(
                attemptId = attemptId,
                onBack = { navController.safePopBackStack() },
            )
        }
    }
}

private const val NAVIGATION_DELAY = 1000L
private const val KEY_LAST_NAV_TIME = "last_nav_time"

fun NavHostController.safePopBackStack(): Boolean {
    val now = System.currentTimeMillis()

    val graphEntry = getBackStackEntry(graph.id)
    val handle = graphEntry.savedStateHandle

    val last = handle.get<Long>(KEY_LAST_NAV_TIME) ?: 0L
    if (now - last < NAVIGATION_DELAY) return false

    handle[KEY_LAST_NAV_TIME] = now
    return popBackStack()
}
