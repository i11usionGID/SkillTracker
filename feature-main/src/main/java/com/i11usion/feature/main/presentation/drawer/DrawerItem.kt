package com.i11usion.feature.main.presentation.drawer

import androidx.compose.ui.graphics.vector.ImageVector
import com.i11usion.core.domain.model.user.Role

data class DrawerItem(
    val screenRoute: String,
    val titleResByRole: Map<Role, Int>,
    val icon: ImageVector,
    val allowedRoles: Set<Role>
)
