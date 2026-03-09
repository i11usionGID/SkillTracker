package com.i11usion.feature.main.presentation.task.create

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.i11usion.core.domain.model.task.TaskStatus
import com.i11usion.core.domain.model.user.User
import com.i11usion.core.ui.di.LocalViewModelFactory
import com.i11usion.core.ui.helpers.asString
import com.i11usion.core.ui.template.ErrorScreen
import com.i11usion.feature.main.R
import kotlinx.coroutines.flow.collectLatest
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun CreateTaskScreen(
    modifier: Modifier = Modifier,
    onCancelClick: () -> Unit
) {
    val viewModel: CreateTaskViewModel = viewModel(factory = LocalViewModelFactory.current)
    val uiState = viewModel.state.collectAsState()
    val state = uiState.value

    val context = LocalContext.current

    LaunchedEffect(viewModel) {
        viewModel.events.collectLatest { event ->
            when (event) {
                CreateTaskEvents.Created -> {
                    onCancelClick()
                }
            }
        }
    }

    when (state) {
        CreateTaskUiState.Initial -> {}
        CreateTaskUiState.Loading -> {
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

        is CreateTaskUiState.Content -> {

            val fsError = state.fullScreenError
            if (fsError != null) {
                ErrorScreen(
                    modifier = modifier.fillMaxSize(),
                    iconResId = fsError.iconResId,
                    cdResId = fsError.cdResId,
                    errorText = fsError.text.asString(context),
                    onRefreshClick = {
                        viewModel.processCommand(CreateTaskCommand.Refresh)
                    }
                )
                return
            }

            var showProgressDialog by remember { mutableStateOf(false) }

            LazyColumn(
                modifier = modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
            ) {
                item {
                    Spacer(modifier = modifier.height(16.dp))

                    CreateTaskHeader()

                    Spacer(modifier = modifier.height(16.dp))
                }

                item {
                    CreateTaskCard(
                        queryTaskName = state.taskNameQuery,
                        queryTaskDescription = state.taskDescriptionQuery,
                        employees = state.employeeList,
                        selectedEmployeeId = state.selectedEmployeeId,
                        selectedStatus = state.selectedStatus,
                        deadlineMillis = state.deadlineMillis,
                        progress = state.progress,
                        canCreate = state.canCreate,
                        isCreating = state.isCreating,
                        onTaskNameQueryChanged = {
                            viewModel.processCommand(
                                CreateTaskCommand.ChangeTaskNameQuery(
                                    it
                                )
                            )
                        },
                        onTaskDescriptionQueryChanged = {
                            viewModel.processCommand(
                                CreateTaskCommand.ChangeTaskDescriptionQuery(
                                    it
                                )
                            )
                        },
                        onStatusChanged = {
                            viewModel.processCommand(
                                CreateTaskCommand.ChangeStatus(
                                    it
                                )
                            )
                        },
                        onEmployeeChanged = {
                            viewModel.processCommand(
                                CreateTaskCommand.ChangeEmployee(
                                    it
                                )
                            )
                        },
                        onDeadlineChanged = {
                            viewModel.processCommand(
                                CreateTaskCommand.ChangeDeadline(
                                    it
                                )
                            )
                        },
                        onProgressClick = { showProgressDialog = true },
                        onCancelClick = onCancelClick,
                        onCreateTaskClick = {
                            viewModel.processCommand(CreateTaskCommand.CreateTask)
                        }
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                }
            }

            if (showProgressDialog) {
                UpdateProgressDialog(
                    progress = state.progress,
                    onProgressChange = {
                        viewModel.processCommand(
                            CreateTaskCommand.ChangeProgress(
                                it
                            )
                        )
                    },
                    onDismiss = { showProgressDialog = false },
                    onSaveClick = { showProgressDialog = false }
                )
            }
        }

        is CreateTaskUiState.Error -> {
            ErrorScreen(
                modifier = modifier.fillMaxSize(),
                iconResId = state.model.iconResId,
                cdResId = state.model.cdResId,
                errorText = state.model.text.asString(context),
                onRefreshClick = {
                    viewModel.processCommand(CreateTaskCommand.Refresh)
                }
            )
        }
    }
}

@Composable
private fun CreateTaskHeader(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(R.string.create_task),
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = stringResource(R.string.create_new_task_and_send_employee),
            fontSize = 18.sp,
            color = MaterialTheme.colorScheme.surfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun CreateTaskCard(
    modifier: Modifier = Modifier,
    queryTaskName: String,
    queryTaskDescription: String,
    employees: List<User>,
    selectedEmployeeId: Int?,
    deadlineMillis: Long?,
    selectedStatus: TaskStatus,
    progress: Int,
    canCreate: Boolean,
    isCreating: Boolean,
    onTaskNameQueryChanged: (String) -> Unit,
    onTaskDescriptionQueryChanged: (String) -> Unit,
    onEmployeeChanged: (Int?) -> Unit,
    onDeadlineChanged: (Long?) -> Unit,
    onStatusChanged: (TaskStatus) -> Unit,
    onProgressClick: () -> Unit,
    onCancelClick: () -> Unit,
    onCreateTaskClick: () -> Unit
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
        Column(modifier = Modifier.padding(vertical = 24.dp, horizontal = 16.dp)) {

            Text(
                text = stringResource(R.string.task_name),
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(8.dp))

            InputField(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 56.dp, max = 120.dp),
                query = queryTaskName,
                placeHolderText = stringResource(R.string.example_develop_new_function),
                onQueryChanged = onTaskNameQueryChanged
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = stringResource(R.string.description),
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(8.dp))

            InputField(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 120.dp, max = 800.dp),
                query = queryTaskDescription,
                placeHolderText = stringResource(R.string.describe_task),
                onQueryChanged = onTaskDescriptionQueryChanged
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = stringResource(R.string.appoint_employee),
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(8.dp))

            val employeeOptions: List<Pair<String, Int>> =
                employees.map { it.name + " @${it.username}" to it.id }

            val selectedEmployeeTitle: String? =
                employeeOptions.firstOrNull { it.second == selectedEmployeeId }?.first

            DropdownField(
                options = employeeOptions.map { it.first },
                selected = selectedEmployeeTitle,
                placeholder = stringResource(R.string.choose_employee),
                onSelectedChange = { title ->
                    val id = employeeOptions.first { it.first == title }.second
                    onEmployeeChanged(id)
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = stringResource(R.string.deadline),
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(8.dp))

            DateField(
                selectedDateMillis = deadlineMillis,
                onDateSelected = { onDeadlineChanged(it) }
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
                statusOptions.first { it.value == selectedStatus }.title

            DropdownField(
                options = statusOptions.map { it.title },
                selected = selectedStatusTitle,
                placeholder = "",
                onSelectedChange = { title ->
                    val status = statusOptions.first { it.title == title }.value
                    onStatusChanged(status)
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = stringResource(R.string.progress_percent),
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(8.dp))

            Box(
                modifier = modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        MaterialTheme.colorScheme.onPrimary,
                        shape = RoundedCornerShape(16.dp)
                    )
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.surfaceBright,
                        shape = RoundedCornerShape(16.dp)
                    )
                    .clickable {
                        onProgressClick()
                    },
                contentAlignment = Alignment.CenterStart
            ) {
                Text(
                    text = progress.toString(),
                    modifier = Modifier.padding(start = 16.dp),
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth()
            ) {
                Button(
                    modifier = Modifier
                        .weight(1f)
                        .height(64.dp),
                    onClick = onCancelClick,
                    colors = ButtonDefaults.buttonColors()
                        .copy(containerColor = MaterialTheme.colorScheme.surfaceBright),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        text = stringResource(R.string.cancel),
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Button(
                    modifier = Modifier
                        .weight(1f)
                        .height(64.dp),
                    onClick = onCreateTaskClick,
                    enabled = canCreate && !isCreating,
                    colors = ButtonDefaults.buttonColors()
                        .copy(
                            containerColor = MaterialTheme.colorScheme.primary,
                            disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                            disabledContentColor = Color.White.copy(alpha = 0.3f)
                        ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    if (!isCreating) {
                        Text(
                            text = stringResource(R.string.create),
                            fontSize = 18.sp,
                            color = Color.White
                        )
                    }

                    if (isCreating) {
                        CircularProgressIndicator(
                            strokeWidth = 2.dp,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun InputField(
    modifier: Modifier = Modifier,
    query: String,
    placeHolderText: String,
    onQueryChanged: (String) -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    OutlinedTextField(
        value = query,
        onValueChange = onQueryChanged,
        placeholder = {
            if (!isFocused) {
                Text(
                    text = placeHolderText,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.surfaceBright
                )
            }
        },
        modifier = modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        interactionSource = interactionSource,
        colors = OutlinedTextFieldDefaults.colors(
            unfocusedBorderColor = MaterialTheme.colorScheme.surfaceBright,
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedPlaceholderColor = MaterialTheme.colorScheme.surfaceBright,
        )
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DropdownField(
    modifier: Modifier = Modifier,
    options: List<String>,
    selected: String?,
    placeholder: String,
    onSelectedChange: (String) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = selected ?: "",
            onValueChange = {},
            readOnly = true,
            singleLine = true,
            placeholder = {
                Text(text = placeholder)
            },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth(),
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded)
            },
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateField(
    modifier: Modifier = Modifier,
    selectedDateMillis: Long?,
    onDateSelected: (Long) -> Unit,
) {
    var openDialog by remember { mutableStateOf(false) }

    val todayUtcStart = remember {
        val now = java.time.Instant.now()
        val utcDate = now.atZone(java.time.ZoneOffset.UTC).toLocalDate()
        utcDate.atStartOfDay(java.time.ZoneOffset.UTC).toInstant().toEpochMilli()
    }

    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = selectedDateMillis,
        selectableDates = object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                return utcTimeMillis >= todayUtcStart
            }
        }
    )

    val formatted = remember(selectedDateMillis) {
        selectedDateMillis?.let { millis ->
            val formatter = SimpleDateFormat("dd MMM yyyy", Locale("ru"))
            formatter.format(Date(millis))
        } ?: ""
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.onPrimary, shape = RoundedCornerShape(16.dp))
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.surfaceBright,
                shape = RoundedCornerShape(16.dp)
            )
            .clickable {
                openDialog = true
            },
        contentAlignment = Alignment.CenterStart
    ) {
        Text(
            text = formatted.ifBlank {
                stringResource(R.string.choose_date)
            },
            modifier = Modifier.padding(start = 16.dp),
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onSurface
        )
    }

    if (openDialog) {
        DatePickerDialog(
            onDismissRequest = { openDialog = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        val picked = datePickerState.selectedDateMillis
                        if (picked != null) onDateSelected(picked)
                        openDialog = false
                    }
                ) { Text(stringResource(R.string.done)) }
            },
            dismissButton = {
                TextButton(onClick = {
                    openDialog = false
                }) { Text(stringResource(R.string.cancel)) }
            },
            colors = DatePickerDefaults.colors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            DatePicker(
                state = datePickerState,
                showModeToggle = false,
                colors = DatePickerDefaults.colors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    selectedDayContainerColor = MaterialTheme.colorScheme.primary,
                    selectedDayContentColor = MaterialTheme.colorScheme.onPrimary,
                    todayDateBorderColor = MaterialTheme.colorScheme.primary,
                    todayContentColor = MaterialTheme.colorScheme.primary,
                    dayContentColor = MaterialTheme.colorScheme.onSurface,
                    navigationContentColor = MaterialTheme.colorScheme.onSurface,
                    headlineContentColor = MaterialTheme.colorScheme.onSurface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    weekdayContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun UpdateProgressDialog(
    progress: Int,
    onProgressChange: (Int) -> Unit,
    onDismiss: () -> Unit,
    onSaveClick: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(20.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.set_progress),
                    style = MaterialTheme.typography.titleLarge
                )

                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = stringResource(R.string.cd_close)
                    )
                }
            }
        },
        text = {
            Column {
                Text(
                    text = stringResource(R.string.progress) + ": $progress%",
                    style = MaterialTheme.typography.bodyLarge
                )

                Spacer(Modifier.height(16.dp))

                Slider(
                    value = progress.toFloat(),
                    onValueChange = { onProgressChange(it.toInt()) },
                    modifier = Modifier.fillMaxWidth(),
                    valueRange = 0f..100f,
                    colors = SliderDefaults.colors(
                        activeTrackColor = Color.Transparent,
                        inactiveTrackColor = Color.Transparent,
                        thumbColor = Color.Transparent,
                        activeTickColor = Color.Transparent,
                        inactiveTickColor = Color.Transparent
                    ),
                    thumb = {
                        Box(
                            modifier = Modifier
                                .size(30.dp)
                                .clip(CircleShape)
                                .background(
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    shape = CircleShape
                                )
                                .border(
                                    1.dp,
                                    MaterialTheme.colorScheme.surfaceVariant,
                                    CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(20.dp)
                                    .background(MaterialTheme.colorScheme.primary, CircleShape)
                            )
                        }
                    },
                    track = { _ ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(100))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(progress / 100f)
                                .height(6.dp)
                                .clip(RoundedCornerShape(100))
                                .background(MaterialTheme.colorScheme.primary)
                        )
                    }
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onSaveClick,
                modifier = Modifier.height(32.dp),
                colors = ButtonDefaults.buttonColors()
                    .copy(containerColor = MaterialTheme.colorScheme.primary),
                shape = RoundedCornerShape(32.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp)
            ) {
                Text(
                    text = stringResource(R.string.save),
                    fontSize = 16.sp,
                    color = Color.White
                )
            }
        }
    )
}


private data class StatusOption(val title: String, val value: TaskStatus)

private fun statusOptions(): List<StatusOption> = listOf(
    StatusOption("Ожидает", TaskStatus.PENDING),
    StatusOption("В работе", TaskStatus.IN_PROGRESS),
    StatusOption("Выполнено", TaskStatus.DONE),
)