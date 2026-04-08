package com.i11usion.feature.main.presentation.notification

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.i11usion.core.domain.model.notification.NotificationType
import com.i11usion.core.navigation.HomeRoutes
import com.i11usion.core.ui.di.LocalViewModelFactory
import com.i11usion.core.ui.helpers.asString
import com.i11usion.core.ui.template.ErrorScreen
import com.i11usion.feature.main.R

@Composable
fun NotificationScreen(
    navController: NavController,
    modifier: Modifier = Modifier
) {
    val viewModel: NotificationViewModel = viewModel(factory = LocalViewModelFactory.current)
    val uiState = viewModel.state.collectAsState()
    val state = uiState.value

    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is NotificationEvent.NavigateTo -> {
                    navController.navigate(event.route) {
                        popUpTo(HomeRoutes.NOTIFICATIONS) {
                            inclusive = true
                        }
                        launchSingleTop = true
                    }
                }
            }
        }
    }

    when (state) {
        NotificationUiState.Initial -> {}
        NotificationUiState.Loading -> {
            Column(
                modifier = modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator(
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        is NotificationUiState.Content -> {
            val fsError = state.fullScreenError
            if (fsError != null) {
                ErrorScreen(
                    modifier = modifier.fillMaxSize(),
                    iconResId = fsError.iconResId,
                    cdResId = fsError.cdResId,
                    errorText = fsError.text.asString(context),
                    onRefreshClick = viewModel::onErrorRefreshClick
                )
                return
            }

            LazyColumn(
                modifier = modifier
                    .fillMaxSize()
            ) {
                item {
                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = stringResource(R.string.notifications),
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        colors = ButtonDefaults.buttonColors()
                            .copy(
                                containerColor = MaterialTheme.colorScheme.surfaceBright,
                                contentColor = MaterialTheme.colorScheme.onSurface,
                                disabledContainerColor = MaterialTheme.colorScheme.surfaceBright.copy(
                                    alpha = 0.5f
                                ),
                                disabledContentColor = MaterialTheme.colorScheme.onSurface.copy(
                                    alpha = 0.5f
                                )
                            ),
                        shape = RoundedCornerShape(16.dp),
                        onClick = { viewModel.readAllNotifications() },
                        enabled = state.notificationList.isNotEmpty()
                    ) {
                        Icon(
                            painterResource(R.drawable.ic_eye),
                            contentDescription = stringResource(R.string.cd_read_all_notifications),
                            modifier = Modifier.size(20.dp)
                        )

                        Spacer(modifier = Modifier.width(4.dp))

                        Text(
                            text = stringResource(R.string.read_all_notifications),
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }

                if (state.notificationList.isEmpty()) {
                    item {
                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = stringResource(R.string.no_notifications_yet),
                                fontSize = 16.sp,
                                color = MaterialTheme.colorScheme.surfaceVariant
                            )
                        }
                    }
                } else {
                    items(
                        items = state.notificationList,
                        key = { notification -> notification.id }
                    ) { notification ->
                        NotificationCard(
                            type = notification.type,
                            title = notification.title,
                            body = notification.body,
                            isRead = notification.isRead,
                            createdAt = notification.createdAt,
                            onCardClick = {
                                viewModel.onNotificationClick(notification)
                            }
                        )
                    }
                }
            }
            if (state.isLoadingOverlay) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            }
        }

        is NotificationUiState.Error -> {
            ErrorScreen(
                modifier = modifier.fillMaxSize(),
                iconResId = state.model.iconResId,
                cdResId = state.model.cdResId,
                errorText = state.model.text.asString(context),
                onRefreshClick = viewModel::onErrorRefreshClick
            )
        }
    }
}

@Composable
fun NotificationCard(
    modifier: Modifier = Modifier,
    type: NotificationType,
    title: String,
    body: String?,
    isRead: Boolean,
    createdAt: String,
    onCardClick: () -> Unit
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable {
                onCardClick()
            },
        colors = CardDefaults.cardColors(
            containerColor = if (isRead) {
                MaterialTheme.colorScheme.onPrimary
            } else {
                MaterialTheme.colorScheme.primaryFixed
            }
        ),
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.surfaceBright
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 24.dp, horizontal = 12.dp)
        ) {
            val readColor: Color
            val readContainerColor: Color
            val notificationIconResId: Int

            if (isRead) {
                readColor = MaterialTheme.colorScheme.onSurface
                readContainerColor = MaterialTheme.colorScheme.surfaceBright
            } else {
                readColor = MaterialTheme.colorScheme.primary
                readContainerColor = MaterialTheme.colorScheme.primaryContainer
            }

            when (type) {
                NotificationType.TASK_ASSIGNED -> {
                    notificationIconResId = R.drawable.ic_folder
                }

                NotificationType.QUIZ_ASSIGNED -> {
                    notificationIconResId = R.drawable.ic_list
                }

                NotificationType.ATTEMPT_COMPLETED -> {
                    notificationIconResId = com.i11usion.core.ui.R.drawable.ic_logo
                }

                NotificationType.COMMENT_ADDED -> {
                    notificationIconResId = R.drawable.ic_comment
                }

                NotificationType.ATTACHMENT_ADDED -> {
                    notificationIconResId = R.drawable.ic_attachment
                }

                NotificationType.TASK_PROGRESS_UPDATED -> {
                    notificationIconResId = R.drawable.ic_trending_up
                }

                NotificationType.QUIZ_AI_READY -> {
                    notificationIconResId = R.drawable.ic_sparkles
                }

                NotificationType.QUIZ_AI_FAILED -> {
                    notificationIconResId = R.drawable.ic_fail
                }
            }

            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(
                        color = readContainerColor,
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(notificationIconResId),
                    contentDescription = stringResource(R.string.cd_notification),
                    modifier = Modifier.size(24.dp),
                    tint = readColor
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(8.dp))

                if (!body.isNullOrBlank()) {
                    Text(
                        text = body,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.surfaceVariant
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                }

                Text(
                    text = createdAt,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.surfaceVariant
                )
            }

            if (!isRead) {
                Box(
                    modifier = Modifier.size(10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary)
                    )
                }
            } else {
                Spacer(modifier = Modifier.size(10.dp))
            }
        }
    }
}