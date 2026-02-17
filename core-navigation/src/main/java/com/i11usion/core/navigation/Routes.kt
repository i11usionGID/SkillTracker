package com.i11usion.core.navigation

object RootRoute {
    const val AUTH = "auth"
    const val HOME = "home"
}

object HomeRoutes {
    val TOP_LEVEL = setOf(
        DASHBOARD,
        TASKS,
        USERS,
        TESTS
    )
    const val DASHBOARD = "home/dashboard"
    const val TASKS = "home/tasks"
    const val USERS = "home/users"
    const val TESTS = "home/tests"
    const val TASK_DETAIL_ROUTE = "home/tasks/detail/{taskId}"
    const val CREATE_TASK = "home/tasks/create"
    const val CREATE_USER = "home/users/create"
    const val CREATE_QUIZ = "home/tests/create"
    const val QUIZ_DETAIL = "home/tests/detail/{quizId}"
    const val QUESTIONS = "home/tests/questions"
    const val CREATE_QUESTION = "home/tests/questions/create_question"
    const val UPDATE_QUESTION = "home/tests/questions/update_question/{questionId}"
    const val TESTING = "home/tests/testing/{quizId}"

    fun taskDetail(taskId: Int) = "home/tasks/detail/$taskId"
    fun updateQuestion(questionId: Int) = "home/tests/questions/update_question/$questionId"
    fun quizDetail(quizId: Int) = "home/tests/detail/$quizId"
    fun quizTesting(quizId: Int) = "home/tests/testing/$quizId"
}
