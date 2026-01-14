package com.i11usion.skilltracker.presentation.screen.home.drawer

import androidx.compose.ui.graphics.vector.ImageVector
import com.i11usion.skilltracker.domain.model.Role
import com.i11usion.skilltracker.navigation.Screen

data class DrawerItem(
    val screen: Screen,
    val title: String,
    val icon: ImageVector,
    val allowedRoles: Set<Role>
)
