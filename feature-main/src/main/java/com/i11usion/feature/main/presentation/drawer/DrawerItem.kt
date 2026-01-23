package com.i11usion.feature.main.presentation.drawer

import androidx.compose.ui.graphics.vector.ImageVector
import com.i11usion.core.domain.model.Role
import com.i11usion.core.navigation.Screen

data class DrawerItem(
    val screen: Screen,
    val title: String,
    val icon: ImageVector,
    val allowedRoles: Set<Role>
)
