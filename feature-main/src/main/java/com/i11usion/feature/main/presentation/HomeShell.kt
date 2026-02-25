package com.i11usion.feature.main.presentation

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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
    isDarkTheme: Boolean,
    state: HomeState.Content,
    onDrawerItemClick: (String) -> Unit,
    onLogoutClick: () -> Unit,
    onNotificationClick: () -> Unit,
    onSwitchThemeClick: () -> Unit,
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
                    theme = isDarkTheme,
                    notificationCount = state.notificationCount,
                    onMenuClick = {
                        scope.launch {
                            if (drawerState.isOpen) {
                                drawerState.close()
                            } else {
                                drawerState.open()
                            }
                        }
                    },
                    onNotificationClick = onNotificationClick,
                    onSwitchThemeClick = onSwitchThemeClick,
                    onLogoutClick = onLogoutClick
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.surface
    ) { innerPadding ->
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
            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                color = MaterialTheme.colorScheme.surface
            ) {
                content()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HomeTopBar(
    theme: Boolean,
    notificationCount: Int,
    onMenuClick: () -> Unit,
    onSwitchThemeClick: () -> Unit,
    onNotificationClick: () -> Unit,
    onLogoutClick: () -> Unit
) {
    TopAppBar(
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Logo(
                    modifier = Modifier
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
            ThemeSwitcher(
                isDark = theme,
                onSwitchThemeClick = onSwitchThemeClick
            )
            CustomIconButton(
                painter = painterResource(com.i11usion.feature.main.R.drawable.ic_notification),
                contentDescription = stringResource(com.i11usion.feature.main.R.string.cd_notifications),
                badgeCount = notificationCount
            ) {
                onNotificationClick()
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
fun ThemeSwitcher(
    modifier: Modifier = Modifier,
    isDark: Boolean,
    onSwitchThemeClick: () -> Unit
) {
    val trackColor by animateColorAsState(
        targetValue = if (isDark)
            Color(0xFF1C1C1E)
        else
            Color(0xFFE0E0E0),
        label = ""
    )

    val borderColor by animateColorAsState(
        targetValue = Color.Transparent,
        label = ""
    )

    val offsetX by animateDpAsState(
        targetValue = if (isDark) 34.dp else 0.dp,
        label = ""
    )

    Box(
        modifier = modifier
            .width(70.dp)
            .height(36.dp)
            .clip(RoundedCornerShape(50))
            .background(trackColor)
            .border(2.dp, borderColor, RoundedCornerShape(50))
            .clickable { onSwitchThemeClick() }
            .padding(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .offset(x = offsetX)
                .clip(CircleShape)
                .background(Color.Black),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (isDark)
                    Icons.Default.DarkMode
                else
                    Icons.Default.LightMode,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
private fun CustomIconButton(
    modifier: Modifier = Modifier,
    badgeCount: Int? = null,
    painter: Painter,
    contentDescription: String,
    onIconButtonClick: () -> Unit
) {
    Box(modifier = Modifier) {
        IconButton(onClick = onIconButtonClick) {
            Icon(
                painter = painter,
                contentDescription = contentDescription,
                modifier = modifier.size(24.dp),
                tint = MaterialTheme.colorScheme.onSurface
            )
        }

        val count = badgeCount ?: 0
        if (badgeCount != null && count > 0) {
            val badgeText = if (count > 9) "9+" else count.toString()

            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .background(
                        color = MaterialTheme.colorScheme.error,
                        shape = RoundedCornerShape(50)
                    )
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.onPrimary,
                        shape = RoundedCornerShape(50)
                    )
                    .padding(horizontal = 6.dp)
            ) {
                Text(
                    text = badgeText,
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}