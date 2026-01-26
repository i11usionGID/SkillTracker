package com.i11usion.feature.main.presentation.drawer

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.i11usion.core.navigation.Screen
import com.i11usion.feature.main.R

@Composable
fun DrawerContent(
    items: List<DrawerItem>,
    selectedScreen: Screen?,
    onItemClick: (Screen) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxHeight()
            .padding(16.dp)
    ) {
        items.forEach { item ->
            val selected = item.screen == selectedScreen

            NavigationDrawerItem(
                label = {
                    Text(
                        text = item.title,
                        fontWeight = FontWeight.Normal,
                        fontSize = 18.sp
                    )
                },
                icon = {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = stringResource(R.string.cd_menu_item),
                        modifier = Modifier.size(24.dp)
                    )
                },
                selected = selected,
                onClick = { onItemClick(item.screen) },
                shape = if (selected) {
                    RoundedCornerShape(16.dp)
                } else {
                    RectangleShape
                },
                colors = NavigationDrawerItemDefaults.colors(
                    selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                    unselectedContainerColor = Color.Transparent,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurface,
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    }
}
