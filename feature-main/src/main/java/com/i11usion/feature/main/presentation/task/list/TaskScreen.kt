package com.i11usion.feature.main.presentation.task.list

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.i11usion.core.domain.model.task.TaskStatus
import com.i11usion.core.domain.model.user.Role
import com.i11usion.core.domain.model.user.User
import com.i11usion.core.ui.di.LocalViewModelFactory
import com.i11usion.core.ui.helpers.asString
import com.i11usion.core.ui.template.ErrorScreen
import com.i11usion.feature.main.R
import com.i11usion.feature.main.presentation.dashboard.model.TaskUiStatus

@Composable
fun TaskScreen(
    modifier: Modifier = Modifier,
    onCreateTaskClick: () -> Unit,
    onTaskCardClick: (Int) -> Unit,
    onBackClick: () -> Unit

) {
    val viewModel: TaskViewModel = viewModel(factory = LocalViewModelFactory.current)
    val state by viewModel.state.collectAsState()

    val lifecycleOwner = LocalLifecycleOwner.current

    val context = LocalContext.current

    DisposableEffect(lifecycleOwner, viewModel) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.processCommand(TaskCommand.Refresh)
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val fsError = state.fullScreenError
    if (fsError != null) {
        ErrorScreen(
            modifier = modifier.fillMaxSize(),
            iconResId = fsError.iconResId,
            cdResId = fsError.cdResId,
            errorText = fsError.text.asString(context),
            onRefreshClick = {
                viewModel.processCommand(TaskCommand.Refresh)
            },
            onBackClick = onBackClick
        )
        return
    }

    if (state.isScreenLoading) {
        Column(
            modifier = modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CircularProgressIndicator(
                color = MaterialTheme.colorScheme.primary
            )
        }
    } else {
        if (state.user != null) {
            LazyColumn(
                modifier = modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
            ) {
                item {
                    TaskHeader(
                        userRole = state.user!!.role,
                        onCreateTaskClick = onCreateTaskClick
                    )
                }

                item {
                    FilterCard(
                        role = state.user!!.role,
                        query = state.taskFilters.query,
                        selectedStatus = state.taskFilters.status,
                        employees = state.employees,
                        selectedEmployeeId = state.taskFilters.employeeId,
                        onQueryChanged = { viewModel.processCommand(TaskCommand.InputSearchQuery(it)) },
                        onStatusChanged = { viewModel.processCommand(TaskCommand.SelectStatus(it)) },
                        onEmployeeChanged = { viewModel.processCommand(TaskCommand.SelectEmployee(it)) }
                    )

                    Spacer(modifier = Modifier.height(32.dp))
                }

                if (state.isListLoading) {
                    item {
                        Spacer(modifier = Modifier.height(32.dp))
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
                } else {
                    if (state.taskList.isEmpty()) {
                        item {
                            Spacer(modifier = Modifier.height(32.dp))
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.Center,
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.ic_list),
                                    contentDescription = stringResource(R.string.tasks_not_found),
                                    modifier = Modifier.size(64.dp),
                                    tint = MaterialTheme.colorScheme.surfaceVariant
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = stringResource(R.string.tasks_not_found),
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    } else {
                        items(
                            items = state.taskList,
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
                                    statusContainerColor =
                                        MaterialTheme.colorScheme.secondaryContainer
                                }

                                TaskUiStatus.DONE -> {
                                    statusColor = MaterialTheme.colorScheme.tertiary
                                    statusContainerColor =
                                        MaterialTheme.colorScheme.tertiaryContainer
                                }
                            }
                            TaskCard(
                                title = task.title,
                                progress = task.progress,
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
            }
        }
    }
}


@Composable
private fun TaskHeader(
    userRole: Role,
    onCreateTaskClick: () -> Unit
) {
    Spacer(modifier = Modifier.height(16.dp))

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(R.string.tasks),
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.manage_task_and_projects),
            fontSize = 18.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        if (userRole == Role.MANAGER) {
            Spacer(modifier = Modifier.height(16.dp))

            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = onCreateTaskClick,
                colors = ButtonDefaults.buttonColors()
                    .copy(containerColor = MaterialTheme.colorScheme.primary),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    text = stringResource(R.string.create_task),
                    fontSize = 18.sp,
                    color = Color.White
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun FilterCard(
    modifier: Modifier = Modifier,
    role: Role,
    query: String,
    selectedStatus: TaskStatus?,
    employees: List<User>,
    selectedEmployeeId: Int?,
    onQueryChanged: (String) -> Unit,
    onStatusChanged: (TaskStatus?) -> Unit,
    onEmployeeChanged: (Int?) -> Unit,
) {
    Card(
        modifier = modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.onPrimary
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.surfaceBright.copy(alpha = 0.3f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = stringResource(R.string.search),
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))

            val interactionSource = remember { MutableInteractionSource() }
            val isFocused by interactionSource.collectIsFocusedAsState()

            SearchField(
                interactionSource = interactionSource,
                query = query,
                onQueryChanged = onQueryChanged,
                isFocused = isFocused
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = stringResource(R.string.status),
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))

            val statusOptions = statusOptions()
            val selectedStatusTitle =
                statusOptions.firstOrNull { it.value == selectedStatus }?.title
                    ?: statusOptions.first().title

            DropdownField(
                options = statusOptions.map { it.title },
                selected = selectedStatusTitle,
                onSelectedChange = { title ->
                    val status = statusOptions.first { it.title == title }.value
                    onStatusChanged(status)
                }
            )

            if (role == Role.MANAGER) {
                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = stringResource(R.string.employees),
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))

                val employeeOptions: List<Pair<String, Int?>> =
                    listOf("Все сотрудники" to null) +
                            employees.map { u -> (u.name) to u.id }

                val selectedEmployeeTitle =
                    employeeOptions.firstOrNull { it.second == selectedEmployeeId }?.first
                        ?: employeeOptions.first().first

                DropdownField(
                    options = employeeOptions.map { it.first },
                    selected = selectedEmployeeTitle,
                    onSelectedChange = { title ->
                        val id = employeeOptions.first { it.first == title }.second
                        onEmployeeChanged(id)
                    }
                )
            }
        }
    }
}


@Composable
private fun SearchField(
    interactionSource: MutableInteractionSource,
    query: String,
    onQueryChanged: (String) -> Unit,
    isFocused: Boolean,
) {
    OutlinedTextField(
        modifier = Modifier.fillMaxWidth(),
        value = query,
        onValueChange = onQueryChanged,
        interactionSource = interactionSource,
        shape = RoundedCornerShape(20.dp),
        placeholder = {
            if (!isFocused) {
                Text(
                    stringResource(R.string.search_text_task_or_quiz),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    fontSize = 16.sp
                )
            }
        },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Поле для поиска",
                modifier = Modifier.size(24.dp),
                tint = MaterialTheme.colorScheme.surfaceVariant
            )
        },
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.onPrimary,
            unfocusedContainerColor = MaterialTheme.colorScheme.onPrimary,
            unfocusedBorderColor = MaterialTheme.colorScheme.surfaceBright,
            focusedBorderColor = MaterialTheme.colorScheme.surfaceBright,
        )
    )
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DropdownField(
    modifier: Modifier = Modifier,
    options: List<String>,
    selected: String,
    onSelectedChange: (String) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = selected,
            onValueChange = {},
            readOnly = true,
            singleLine = true,
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth(),
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.onPrimary,
                unfocusedContainerColor = MaterialTheme.colorScheme.onPrimary,
                unfocusedBorderColor = MaterialTheme.colorScheme.surfaceBright,
                focusedBorderColor = MaterialTheme.colorScheme.surfaceBright,
            )
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            containerColor = MaterialTheme.colorScheme.onPrimary
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onSelectedChange(option)
                        expanded = false
                    },
                    leadingIcon = {
                        if (option == selected) {
                            Icon(Icons.Default.Check, contentDescription = null)
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun TaskCard(
    modifier: Modifier = Modifier,
    title: String,
    progress: String,
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
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = "$progress%",
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = title,
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(72.dp),
                contentAlignment = Alignment.BottomStart
            ) {
                if (!description.isNullOrBlank()) {
                    Text(
                        text = description,
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        fontSize = 16.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    painter = painterResource(R.drawable.ic_user),
                    contentDescription = stringResource(R.string.cd_employee_task),
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.surfaceVariant
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = employeeName,
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    fontSize = 16.sp
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (!deadline.isNullOrBlank()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        painter = painterResource(R.drawable.ic_calendar),
                        contentDescription = stringResource(R.string.cd_deadline_date),
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.surfaceVariant
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = deadline,
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        fontSize = 16.sp
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
            LineProgress(
                progress = progress
            )
        }
    }
}

@Composable
private fun LineProgress(
    progress: String,
    modifier: Modifier = Modifier,
    height: Dp = 3.dp,
    corner: Dp = 50.dp,
) {
    val progressPercentFloat = (progress.toInt() / 100f).coerceIn(0f, 1f)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .clip(RoundedCornerShape(corner))
            .background(MaterialTheme.colorScheme.surfaceBright)
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(progressPercentFloat)
                .background(MaterialTheme.colorScheme.primary)
        )
    }
}


private data class StatusOption(val title: String, val value: TaskStatus?)

private fun statusOptions(): List<StatusOption> = listOf(
    StatusOption("Все статусы", null),
    StatusOption("Ожидают", TaskStatus.PENDING),
    StatusOption("В работе", TaskStatus.IN_PROGRESS),
    StatusOption("Выполнено", TaskStatus.DONE),
)
