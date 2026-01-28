package com.i11usion.feature.main.presentation.drawer

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Checklist
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.People
import com.i11usion.core.domain.model.Role
import com.i11usion.core.navigation.HomeRoutes
import com.i11usion.feature.main.R

object DrawerConfig {

    val items = listOf(
        DrawerItem(
            screenRoute = HomeRoutes.DASHBOARD,
            titleResByRole = mapOf(
                Role.EMPLOYEE to R.string.dashboard_manager_employee,
                Role.MANAGER to R.string.dashboard_manager_employee
            ),
            icon = Icons.Outlined.Home,
            allowedRoles = setOf(
                Role.EMPLOYEE,
                Role.MANAGER
            )
        ),
        DrawerItem(
            screenRoute = HomeRoutes.TASKS,
            titleResByRole = mapOf(
                Role.EMPLOYEE to R.string.tasks_manager_employee,
                Role.MANAGER to R.string.tasks_manager_employee
            ),
            icon = Icons.Outlined.Checklist,
            allowedRoles = setOf(
                Role.EMPLOYEE,
                Role.MANAGER
            )
        ),
        DrawerItem(
            screenRoute = HomeRoutes.USERS,
            titleResByRole = mapOf(
                Role.MANAGER to R.string.users_manager
            ),
            icon = Icons.Outlined.People,
            allowedRoles = setOf(Role.MANAGER)
        ),
        DrawerItem(
            screenRoute = HomeRoutes.TESTS,
            mapOf(
                Role.EMPLOYEE to R.string.tests_employee,
                Role.MANAGER to R.string.tests_manager
            ),
            icon = Icons.Outlined.Description,
            allowedRoles = setOf(
                Role.EMPLOYEE,
                Role.MANAGER
            )
        )
    )
}

