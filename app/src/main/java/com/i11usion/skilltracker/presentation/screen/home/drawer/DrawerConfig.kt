package com.i11usion.skilltracker.presentation.screen.home.drawer

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Checklist
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.People
import com.i11usion.skilltracker.domain.model.Role
import com.i11usion.skilltracker.navigation.Screen

object DrawerConfig {

    val items = listOf(
        DrawerItem(
            screen = Screen.Home.Dashboard,
            title = "Дашборд",
            icon = Icons.Outlined.Home,
            allowedRoles = setOf(
                Role.EMPLOYEE,
                Role.MANAGER
            )
        ),
        DrawerItem(
            screen = Screen.Home.Tasks,
            title = "Задачи",
            icon = Icons.Outlined.Checklist,
            allowedRoles = setOf(
                Role.EMPLOYEE,
                Role.MANAGER
            )
        ),
        DrawerItem(
            screen = Screen.Home.Users,
            title = "Пользователи",
            icon = Icons.Outlined.People,
            allowedRoles = setOf(Role.MANAGER)
        ),
        DrawerItem(
            screen = Screen.Home.Tests,
            title = "Тесты",
            icon = Icons.Outlined.Description,
            allowedRoles = setOf(
                Role.EMPLOYEE,
                Role.MANAGER
            )
        )
    )
}

