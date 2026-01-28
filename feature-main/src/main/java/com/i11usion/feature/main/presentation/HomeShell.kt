package com.i11usion.feature.main.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.i11usion.core.ui.R
import com.i11usion.core.ui.model.Logo
import com.i11usion.feature.main.presentation.drawer.DrawerContent
import kotlinx.coroutines.launch

@Composable
fun HomeShell(
    state: HomeState.Content,
    onDrawerItemClick: (String) -> Unit,
    onLogoutClick: () -> Unit,
    content: @Composable () -> Unit
) {
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    var topBarHeight by remember { mutableStateOf(0.dp) }
    val density = LocalDensity.current


    Scaffold(
        topBar = {
            Box(
                modifier = Modifier.onSizeChanged { size ->
                    topBarHeight = with(density) { size.height.toDp() }
                }
            ) {
                HomeTopBar(
                    onMenuClick = {
                        scope.launch {
                            if (drawerState.isOpen) {
                                drawerState.close()
                            } else {
                                drawerState.open()
                            }
                        }
                    },
                    onLogoutClick = onLogoutClick
                )
            }
        }
    ) { padding ->
        ModalNavigationDrawer(
            drawerState = drawerState,
            scrimColor = MaterialTheme.colorScheme.scrim.copy(alpha = 0.55f),
            drawerContent = {
                ModalDrawerSheet(
                    modifier = Modifier
                        .padding(top = topBarHeight)
                        .width(280.dp)
                        .background(
                            shape = RoundedCornerShape(0.dp),
                            color = MaterialTheme.colorScheme.onPrimary
                        ),
                    drawerContainerColor = MaterialTheme.colorScheme.onPrimary,
                    drawerContentColor = MaterialTheme.colorScheme.onSurface
                ) {
                    DrawerContent(
                        items = state.drawerItems,
                        selectedRoute = state.selectedScreenRoute,
                        userRole = state.role,
                        onItemClick = { route ->
                            onDrawerItemClick(route)
                            scope.launch { drawerState.close() }
                        }
                    )
                }
            }
        ) {
            Box(Modifier.padding(padding)) { content() }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HomeTopBar(
    onMenuClick: () -> Unit,
    onLogoutClick: () -> Unit
) {
    TopAppBar(
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Logo(
                    boxModifier = Modifier
                        .size(32.dp)
                        .background(
                            color = MaterialTheme.colorScheme.primary,
                            shape = RoundedCornerShape(percent = 30)
                        ),
                    imageModifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.app_name),
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
            }
        },
        modifier = Modifier.border(width = 1.dp, color = MaterialTheme.colorScheme.surface),
        navigationIcon = {
            CustomIconButton(
                painter = painterResource(com.i11usion.feature.main.R.drawable.ic_menu),
                contentDescription = stringResource(com.i11usion.feature.main.R.string.cd_menu)
            ) {
                onMenuClick()
            }
        },
        actions = {
            CustomIconButton(
                painter = painterResource(com.i11usion.feature.main.R.drawable.ic_notification),
                contentDescription = stringResource(com.i11usion.feature.main.R.string.cd_notification)
            ) {
                //дописать
            }
            CustomIconButton(
                painter = painterResource(com.i11usion.feature.main.R.drawable.ic_log_out),
                contentDescription = stringResource(com.i11usion.feature.main.R.string.cd_logout)
            ) {
                onLogoutClick()
            }
        },
        colors = TopAppBarDefaults.topAppBarColors()
            .copy(containerColor = MaterialTheme.colorScheme.onPrimary)
    )
}

@Composable
private fun CustomIconButton(
    modifier: Modifier = Modifier,
    painter: Painter,
    contentDescription: String,
    onIconButtonClick: () -> Unit
) {
    IconButton(onClick = onIconButtonClick) {
        Icon(
            painter = painter,
            contentDescription = contentDescription,
            modifier = modifier.size(24.dp),
            tint = MaterialTheme.colorScheme.onSurface
        )
    }
}