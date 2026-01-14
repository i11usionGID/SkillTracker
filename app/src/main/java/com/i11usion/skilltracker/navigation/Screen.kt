package com.i11usion.skilltracker.navigation

sealed class Screen(val route: String) {

    object Auth : Screen(ROUTE_AUTH)

    object Home : Screen(ROUTE_HOME) {

        object Dashboard : Screen(ROUTE_DASHBOARD)
        object Tasks : Screen(ROUTE_TASKS)
        object Users : Screen(ROUTE_USERS)
        object Tests : Screen(ROUTE_TESTS)
    }

    companion object {
        const val ROUTE_AUTH = "auth"
        const val ROUTE_HOME = "home"
        const val ROUTE_DASHBOARD = "home/dashboard"
        const val ROUTE_TASKS = "home/tasks"
        const val ROUTE_USERS = "home/users"
        const val ROUTE_TESTS = "home/tests"
    }
}
