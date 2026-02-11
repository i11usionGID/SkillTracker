package com.i11usion.core.navigation

object RootRoute {
    const val AUTH = "auth"
    const val HOME = "home"
}

object HomeRoutes {
    const val DASHBOARD = "home/dashboard"
    const val TASKS = "home/tasks"
    const val USERS = "home/users"
    const val TESTS = "home/tests"
    const val TASK_DETAIL = "home/tasks/detail"
    const val TASK_DETAIL_ROUTE = "home/tasks/detail/{taskId}"

    fun taskDetail(taskId: Int) = "home/tasks/detail/$taskId"
}
