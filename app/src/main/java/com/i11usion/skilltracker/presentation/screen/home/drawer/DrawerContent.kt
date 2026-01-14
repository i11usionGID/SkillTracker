package com.i11usion.skilltracker.presentation.screen.home.drawer

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.i11usion.skilltracker.navigation.Screen

@Composable
fun DrawerContent(
    items: List<DrawerItem>,
    onItemClick: (Screen) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxHeight()
            .padding(16.dp)
    ) {
        items.forEach { item ->
            NavigationDrawerItem(
                label = { Text(item.title) },
                icon = { Icon(item.icon, null) },
                selected = false,
                onClick = { onItemClick(item.screen) }
            )
        }
    }
}

