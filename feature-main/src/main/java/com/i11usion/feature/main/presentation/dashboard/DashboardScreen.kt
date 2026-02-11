package com.i11usion.feature.main.presentation.dashboard

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.i11usion.core.domain.model.user.Role
import com.i11usion.core.ui.di.LocalViewModelFactory
import com.i11usion.feature.main.R
import com.i11usion.feature.main.presentation.dashboard.model.TaskUiStatus

@Composable
fun DashboardScreen(
    modifier: Modifier = Modifier,
    onCheckAllTaskClick: () -> Unit,
    onTaskCardClick: (Int) -> Unit
) {
    val viewModel: DashboardViewModel = viewModel(factory = LocalViewModelFactory.current)
    val uiState = viewModel.state.collectAsState()
    val state = uiState.value
    when (state) {
        is DashboardUiState.Content -> {
            val latestTask = state.dashboardData.latest
            LazyColumn(
                modifier = modifier
                    .fillMaxSize()
                    .padding(vertical = 8.dp, horizontal = 16.dp)
            ) {
                item {
                    Text(
                        text = "Добро пожаловать, ${state.user.name}!",
                        fontSize = 32.sp,
                        lineHeight = 40.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (state.user.role == Role.EMPLOYEE) {
                            stringResource(R.string.subtitle_employee)
                        } else {
                            stringResource(
                                R.string.subtitle_manager
                            )
                        },
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }
                item {
                    TaskStatisticCard(
                        titleResId = R.string.total_task,
                        taskCount = state.dashboardData.stats.total,
                        taskCountColor = MaterialTheme.colorScheme.onSurface,
                        iconResId = R.drawable.ic_folder,
                        contentDescriptionResId = R.string.cd_total_task,
                        iconColor = MaterialTheme.colorScheme.primary,
                        iconTintColor = MaterialTheme.colorScheme.primaryContainer
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    TaskStatisticCard(
                        titleResId = R.string.in_work,
                        taskCount = state.dashboardData.stats.inProgress,
                        taskCountColor = MaterialTheme.colorScheme.secondary,
                        iconResId = R.drawable.ic_lightning,
                        contentDescriptionResId = R.string.cd_task_done,
                        iconColor = MaterialTheme.colorScheme.secondary,
                        iconTintColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    TaskStatisticCard(
                        titleResId = R.string.task_done,
                        taskCount = state.dashboardData.stats.done,
                        taskCountColor = MaterialTheme.colorScheme.tertiary,
                        iconResId = com.i11usion.core.ui.R.drawable.ic_logo,
                        contentDescriptionResId = R.string.cd_task_done,
                        iconColor = MaterialTheme.colorScheme.tertiary,
                        iconTintColor = MaterialTheme.colorScheme.tertiaryContainer
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    TaskStatisticCard(
                        titleResId = R.string.task_waitting,
                        taskCount = state.dashboardData.stats.pending,
                        taskCountColor = MaterialTheme.colorScheme.surfaceVariant,
                        iconResId = R.drawable.ic_watch,
                        contentDescriptionResId = R.string.cd_task_waitting,
                        iconColor = MaterialTheme.colorScheme.surfaceVariant,
                        iconTintColor = MaterialTheme.colorScheme.surfaceBright
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                }
                item {
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = stringResource(R.string.latest_tasks),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        Row(
                            modifier = Modifier.clickable { onCheckAllTaskClick() },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                stringResource(R.string.check_all),
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = stringResource(R.string.check_all_tasks),
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
                item {
                    if (latestTask.isEmpty()) {
                        Spacer(modifier = Modifier.height(32.dp))
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = stringResource(R.string.no_task_yet),
                                fontSize = 16.sp,
                                color = MaterialTheme.colorScheme.surfaceVariant
                            )
                        }
                        Spacer(modifier = Modifier.height(32.dp))
                    }
                }
                items(
                    items = latestTask,
                    key = { task -> task.id }
                ) { task ->
                    val statusColor: Color
                    val statusContainerColor: Color
                    when (task.status) {
                        TaskUiStatus.PENDING -> {
                            statusColor = MaterialTheme.colorScheme.surfaceVariant
                            statusContainerColor = MaterialTheme.colorScheme.surfaceBright
                        }

                        TaskUiStatus.IN_PROGRESS -> {
                            statusColor = MaterialTheme.colorScheme.secondary
                            statusContainerColor = MaterialTheme.colorScheme.secondaryContainer
                        }

                        TaskUiStatus.DONE -> {
                            statusColor = MaterialTheme.colorScheme.tertiary
                            statusContainerColor = MaterialTheme.colorScheme.tertiaryContainer
                        }
                    }
                    TaskCard(
                        title = task.title,
                        description = task.description,
                        deadline = task.deadline,
                        employeeName = task.employee.name,
                        statusText = task.statusText,
                        statusColor = statusColor,
                        statusContainerColor = statusContainerColor,
                        onTaskCardClick = { onTaskCardClick(task.id) }
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }

        is DashboardUiState.Error -> {
            Text(text = "Error ${state.message}")
        }

        DashboardUiState.Initial -> {}

        DashboardUiState.Loading -> {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator(
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun TaskStatisticCard(
    modifier: Modifier = Modifier,
    titleResId: Int,
    taskCount: String,
    taskCountColor: Color,
    iconResId: Int,
    contentDescriptionResId: Int,
    iconColor: Color,
    iconTintColor: Color
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.onPrimary,
            disabledContainerColor = MaterialTheme.colorScheme.onPrimary
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 1.dp,
            focusedElevation = 2.dp,
            pressedElevation = 2.dp
        ),
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.surfaceBright.copy(alpha = 0.3f)
        )
    ) {
        Row(
            modifier = Modifier.padding(24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = stringResource(titleResId),
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = taskCount,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = taskCountColor
                )
            }
            Spacer(modifier = Modifier.weight(1f))
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .background(color = iconTintColor, shape = RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(iconResId),
                    contentDescription = stringResource(contentDescriptionResId),
                    modifier = Modifier.size(28.dp),
                    tint = iconColor
                )
            }
        }
    }
}

@Composable
private fun TaskCard(
    modifier: Modifier = Modifier,
    title: String,
    description: String?,
    deadline: String?,
    employeeName: String,
    statusText: String,
    statusColor: Color,
    statusContainerColor: Color,
    onTaskCardClick: () -> Unit
) {
    Card(
        modifier = modifier.clickable(onClick = onTaskCardClick),
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.onPrimary,
            disabledContainerColor = MaterialTheme.colorScheme.onPrimary
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 1.dp,
            focusedElevation = 2.dp,
            pressedElevation = 2.dp
        ),
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.surfaceBright.copy(alpha = 0.3f)
        )
    ) {
        Column(modifier = Modifier.padding(vertical = 24.dp, horizontal = 16.dp)) {

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp
                )
                Spacer(modifier = Modifier.weight(1f))
                Box(
                    modifier = Modifier.background(
                        color = statusContainerColor,
                        shape = RoundedCornerShape(50.dp)
                    ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        text = statusText,
                        color = statusColor,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (!description.isNullOrBlank()) {
                Text(
                    text = description,
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    fontSize = 16.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            Row {
                Text(
                    text = employeeName,
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    fontSize = 16.sp
                )
                if (!deadline.isNullOrBlank()) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "• $deadline",
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        fontSize = 16.sp
                    )
                }
            }
        }
    }
}