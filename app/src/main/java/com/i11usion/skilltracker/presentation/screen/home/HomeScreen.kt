package com.i11usion.skilltracker.presentation.screen.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ExitToApp
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import com.i11usion.skilltracker.navigation.Screen
import com.i11usion.skilltracker.presentation.screen.home.drawer.DrawerContent
import kotlinx.coroutines.launch

@Composable
fun HomeScreen(
    state: HomeUiState,
    onDrawerItemClick: (Screen) -> Unit,
    onLogoutClick: () -> Unit
) {
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            DrawerContent(
                items = state.drawerItems,
                onItemClick = { screen ->
                    onDrawerItemClick(screen)
                    scope.launch { drawerState.close() }
                }
            )
        }
    ) {
        Scaffold(
            topBar = {
                HomeTopBar(
                    userName = state.userName,
                    onMenuClick = {
                        scope.launch { drawerState.open() }
                    },
                    onLogoutClick = onLogoutClick
                )
            }
        ) { padding ->
            Box(Modifier.padding(padding)) {
                Text("Контент")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeTopBar(
    userName: String,
    onMenuClick: () -> Unit,
    onLogoutClick: () -> Unit
) {
    TopAppBar(
        title = {
            Text(
                text = userName,
                color = MaterialTheme.colorScheme.onPrimary
            )
        },
        navigationIcon = {
            IconButton(onClick = onMenuClick) {
                Icon(
                    imageVector = Icons.Outlined.Menu,
                    contentDescription = "Menu"
                )
            }
        },
        actions = {
            IconButton(onClick = onLogoutClick) {
                Icon(
                    imageVector = Icons.Outlined.ExitToApp,
                    contentDescription = "Logout"
                )
            }
        }
    )
}

