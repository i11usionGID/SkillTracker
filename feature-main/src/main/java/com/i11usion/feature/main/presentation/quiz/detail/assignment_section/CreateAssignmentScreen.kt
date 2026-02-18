package com.i11usion.feature.main.presentation.quiz.detail.assignment_section

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
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
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.i11usion.core.ui.di.LocalViewModelFactory
import com.i11usion.feature.main.R
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.abs


@Composable
fun CreateAssignmentScreen(
    modifier: Modifier = Modifier,
    quizId: Int,
    onCancelClick: () -> Unit,
) {
    val viewModel: CreateAssignmentViewModel =
        viewModel(factory = LocalViewModelFactory.current)
    val state by viewModel.state.collectAsState()

    LaunchedEffect(quizId) {
        viewModel.processCommand(
            CreateAssignmentSectionCommand.Init(
                quizId = quizId
            )
        )
    }

    LaunchedEffect(viewModel) {
        viewModel.events.collectLatest { event ->
            when (event) {
                CreateAssignmentSectionEvent.Close -> onCancelClick()
                CreateAssignmentSectionEvent.Created -> onCancelClick()
                is CreateAssignmentSectionEvent.ShowError -> {
                    // сюда можно подключить SnackbarHostState
                }
            }
        }
    }

    when (val s = state) {
        CreateAssignmentSectionUiState.Initial -> Unit

        CreateAssignmentSectionUiState.Loading -> Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) { CircularProgressIndicator() }

        is CreateAssignmentSectionUiState.Error -> Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) { Text(s.message) }

        is CreateAssignmentSectionUiState.Content -> CreateAssignmentSectionContent(
            modifier = modifier,
            state = s,
            onCommand = viewModel::processCommand
        )
    }
}

@Composable
private fun CreateAssignmentSectionContent(
    modifier: Modifier,
    state: CreateAssignmentSectionUiState.Content,
    onCommand: (CreateAssignmentSectionCommand) -> Unit
) {
    var showMaxAttemptsDialog by remember { mutableStateOf(false) }
    var showCooldownDialog by remember { mutableStateOf(false) }

    var tempMaxAttempts by remember(state.maxAttempts) {
        mutableIntStateOf((state.maxAttempts ?: 1).coerceIn(1, 10))
    }
    var tempCooldown by remember(state.cooldownHours) {
        mutableIntStateOf(state.cooldownHours.coerceIn(0, 24))
    }

    val selectedUserLabel = remember(state.selectedUserId, state.employees) {
        state.employees.firstOrNull { it.id == state.selectedUserId }?.label
    }
    val selectedTaskLabel = remember(state.selectedTaskId, state.tasks) {
        state.tasks.firstOrNull { it.id == state.selectedTaskId }?.label
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        item {
            Spacer(Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Создать назначение теста",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(Modifier.height(16.dp))
        }

        item {
            // Инфо блок как на макете (можешь удалить, если не нужно)
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primary.copy(
                        alpha = 0.08f
                    )
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.18f))
            ) {
                Text(
                    modifier = Modifier.padding(16.dp),
                    text = "Вы можете назначить тест либо пользователю напрямую, либо связать с задачей. При привязке к задаче, тест автоматически назначится исполнителю задачи.",
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 16.sp
                )
            }
            Spacer(Modifier.height(16.dp))
        }

        item {
            Card(
                shape = RoundedCornerShape(32.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.onPrimary),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                border = BorderStroke(
                    1.dp,
                    MaterialTheme.colorScheme.surfaceBright.copy(alpha = 0.3f)
                )
            ) {
                Column(Modifier.padding(vertical = 24.dp, horizontal = 24.dp)) {

                    Text(
                        "Пользователь",
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 16.sp
                    )
                    Spacer(Modifier.height(8.dp))

                    DropdownFieldByOptions(
                        options = state.employees,
                        selectedLabel = selectedUserLabel,
                        placeholder = "Выберите пользователя",
                        enabled = !state.isCreating,
                        onSelectedId = { onCommand(CreateAssignmentSectionCommand.SelectUser(it)) }
                    )

                    Spacer(Modifier.height(16.dp))

                    Text(
                        "Или привязать к задаче",
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 16.sp
                    )
                    Spacer(Modifier.height(8.dp))

                    DropdownFieldByOptions(
                        options = state.tasks,
                        selectedLabel = selectedTaskLabel,
                        placeholder = "Без привязки к задаче",
                        enabled = !state.isCreating,
                        onSelectedId = { onCommand(CreateAssignmentSectionCommand.SelectTask(it)) }
                    )

                    Spacer(Modifier.height(16.dp))

                    Text(
                        "Срок выполнения",
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 16.sp
                    )
                    Spacer(Modifier.height(8.dp))

                    DateFieldWithReset(
                        selectedDateMillis = state.dueDateMillis,
                        enabled = !state.isCreating,
                        onDateSelected = {
                            onCommand(
                                CreateAssignmentSectionCommand.SetDueDateMillis(
                                    it
                                )
                            )
                        },
                        onReset = { onCommand(CreateAssignmentSectionCommand.ResetDueDate) }
                    )

                    Spacer(Modifier.height(16.dp))

                    Text(
                        "Максимум попыток",
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 16.sp
                    )
                    Spacer(Modifier.height(8.dp))

                    WheelFieldWithReset(
                        text = state.maxAttempts?.toString().orEmpty(),
                        placeholder = "Без ограничения",
                        enabled = !state.isCreating,
                        onClick = {
                            tempMaxAttempts = (state.maxAttempts ?: 1).coerceIn(1, 10)
                            showMaxAttemptsDialog = true
                        },
                        onReset = { onCommand(CreateAssignmentSectionCommand.ResetMaxAttempts) }
                    )

                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = "Оставьте пустым для неограниченного количества попыток",
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        fontSize = 14.sp
                    )

                    Spacer(Modifier.height(16.dp))

                    Text(
                        "Пауза между попытками (часы)",
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 16.sp
                    )
                    Spacer(Modifier.height(8.dp))

                    ClickField(
                        text = state.cooldownHours.toString(),
                        placeHolderText = "0",
                        isFocused = false,
                        enabled = !state.isCreating,
                        onClickFieldClick = {
                            tempCooldown = state.cooldownHours.coerceIn(0, 24)
                            showCooldownDialog = true
                        }
                    )

                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = "Время ожидания перед следующей попыткой (0 = без паузы)",
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        fontSize = 14.sp
                    )

                    Spacer(Modifier.height(16.dp))

                    CheckField(
                        checked = state.required,
                        title = "Обязательное прохождение",
                        onToggle = { onCommand(CreateAssignmentSectionCommand.ToggleRequired) }
                    )

                    Spacer(Modifier.height(16.dp))

                    Row(Modifier.fillMaxWidth()) {
                        Button(
                            modifier = Modifier
                                .weight(1f)
                                .height(64.dp),
                            onClick = { onCommand(CreateAssignmentSectionCommand.CloseClicked) },
                            colors = ButtonDefaults.buttonColors()
                                .copy(containerColor = MaterialTheme.colorScheme.surfaceBright),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Text(
                                "Отмена",
                                fontSize = 18.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Spacer(Modifier.width(8.dp))

                        Button(
                            modifier = Modifier
                                .weight(1f)
                                .height(64.dp),
                            onClick = { onCommand(CreateAssignmentSectionCommand.CreateClicked) },
                            enabled = state.canCreate,
                            colors = ButtonDefaults.buttonColors().copy(
                                containerColor = MaterialTheme.colorScheme.primary,
                                disabledContainerColor = MaterialTheme.colorScheme.primary.copy(
                                    alpha = 0.3f
                                )
                            ),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            if (!state.isCreating) {
                                Text(
                                    "Создать",
                                    fontSize = 18.sp,
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                            } else {
                                CircularProgressIndicator(
                                    strokeWidth = 2.dp,
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showMaxAttemptsDialog) {
        NumberWheelDialog(
            title = "Максимум попыток",
            value = tempMaxAttempts.coerceIn(1, 10),
            range = 1..10,
            label = "Попытки",
            onValueChange = { tempMaxAttempts = it.coerceIn(1, 10) },
            onDismiss = { showMaxAttemptsDialog = false },
            onSaveClick = {
                showMaxAttemptsDialog = false
                onCommand(CreateAssignmentSectionCommand.ChangeMaxAttempts(tempMaxAttempts))
            }
        )
    }

    if (showCooldownDialog) {
        NumberWheelDialog(
            title = "Пауза между попытками",
            value = tempCooldown.coerceIn(0, 24),
            range = 0..24,
            label = "Часы",
            onValueChange = { tempCooldown = it.coerceIn(0, 24) },
            onDismiss = { showCooldownDialog = false },
            onSaveClick = {
                showCooldownDialog = false
                onCommand(CreateAssignmentSectionCommand.ChangeCooldownHours(tempCooldown))
            }
        )
    }
}

/* -------------------- Dropdown по options(id/label) -------------------- */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun <T> DropdownFieldByOptions(
    options: List<T>,
    selectedLabel: String?,
    placeholder: String,
    enabled: Boolean,
    onSelectedId: (Int?) -> Unit,
) where T : Any {
    // поддерживаем два типа options: EmployeeOption / TaskOption
    val labels = remember(options) {
        when {
            options.firstOrNull() is EmployeeOption ->
                (options as List<EmployeeOption>).map { it.label }

            options.firstOrNull() is TaskOption ->
                (options as List<TaskOption>).map { it.label }

            else -> emptyList()
        }
    }

    DropdownField(
        options = labels,
        selected = selectedLabel,
        placeholder = placeholder,
        onSelectedChange = { label ->
            val id = when {
                options.firstOrNull() is EmployeeOption ->
                    (options as List<EmployeeOption>).firstOrNull { it.label == label }?.id

                options.firstOrNull() is TaskOption ->
                    (options as List<TaskOption>).firstOrNull { it.label == label }?.id

                else -> null
            }
            onSelectedId(id)
        },
        modifier = Modifier.alpha(if (enabled) 1f else 0.55f)
    )
}

/* -------------------- DateField + reset справа -------------------- */

@Composable
private fun DateFieldWithReset(
    selectedDateMillis: Long?,
    enabled: Boolean,
    onDateSelected: (Long) -> Unit,
    onReset: () -> Unit
) {
    Box(modifier = Modifier.fillMaxWidth()) {
        DateField(
            modifier = Modifier
                .fillMaxWidth()
                .alpha(if (enabled) 1f else 0.55f),
            selectedDateMillis = selectedDateMillis,
            onDateSelected = onDateSelected
        )

        IconButton(
            onClick = onReset,
            enabled = enabled && selectedDateMillis != null,
            modifier = Modifier.align(Alignment.CenterEnd)
        ) {
            Icon(
                imageVector = Icons.Outlined.Refresh,
                contentDescription = null,
                tint = if (selectedDateMillis != null) MaterialTheme.colorScheme.surfaceVariant
                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
            )
        }
    }
}

/* -------------------- Wheel поле + reset справа -------------------- */

@Composable
private fun WheelFieldWithReset(
    text: String,
    placeholder: String,
    enabled: Boolean,
    onClick: () -> Unit,
    onReset: () -> Unit
) {
    Box(modifier = Modifier.fillMaxWidth()) {
        ClickField(
            text = text,
            placeHolderText = placeholder,
            isFocused = false,
            enabled = enabled,
            onClickFieldClick = onClick
        )

        IconButton(
            onClick = onReset,
            enabled = enabled && text.isNotBlank(),
            modifier = Modifier.align(Alignment.CenterEnd)
        ) {
            Icon(
                imageVector = Icons.Outlined.Close,
                contentDescription = null,
                tint = if (text.isNotBlank()) MaterialTheme.colorScheme.surfaceVariant
                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
            )
        }
    }
}

/* -------------------- CheckField (как у тебя) -------------------- */

@Composable
private fun CheckField(
    modifier: Modifier = Modifier,
    checked: Boolean,
    title: String,
    onToggle: () -> Unit
) {
    Row(modifier = modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Checkbox(checked = checked, onCheckedChange = { onToggle() })
        Spacer(Modifier.width(8.dp))
        Column {
            Text(text = title, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)
            Text(
                text = "Тест помечается как обязательный к выполнению",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.surfaceVariant
            )
        }
    }
}

/* -------------------- DropdownField (nullable selected + placeholder) -------------------- */

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
            placeholder = { Text(text = placeholder) },
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

/* -------------------- DateField -------------------- */

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
            .clickable { openDialog = true },
        contentAlignment = Alignment.CenterStart
    ) {
        Text(
            text = formatted.ifBlank { stringResource(R.string.choose_date) },
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

/* -------------------- ClickField (used by wheel fields) -------------------- */

@Composable
private fun ClickField(
    modifier: Modifier = Modifier,
    text: String,
    placeHolderText: String,
    isFocused: Boolean,
    enabled: Boolean,
    onClickFieldClick: () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp)
            .clip(RoundedCornerShape(20.dp))
            .border(
                width = 1.dp,
                shape = RoundedCornerShape(20.dp),
                color = if (isFocused) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceBright
            )
            .alpha(if (enabled) 1f else 0.55f)
            .clickable(enabled = enabled, onClick = onClickFieldClick),
        contentAlignment = Alignment.TopStart
    ) {
        Text(
            text = text.ifBlank { placeHolderText },
            modifier = Modifier.padding(16.dp),
            fontSize = 16.sp,
            color = if (text.isBlank()) MaterialTheme.colorScheme.surfaceBright else MaterialTheme.colorScheme.onSurface
        )
    }
}

/* -------------------- NumberWheelDialog -------------------- */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NumberWheelDialog(
    title: String,
    value: Int,
    range: IntRange,
    label: String,
    onValueChange: (Int) -> Unit,
    onDismiss: () -> Unit,
    onSaveClick: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(20.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        title = {
            // ✅ без крестика
            Text(title, style = MaterialTheme.typography.titleLarge)
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally // ✅ центрируем контент
            ) {
                Text(
                    value.toString(),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.height(12.dp))

                // ✅ чтобы наверняка колесо было по центру даже если внутри fixed width
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    WheelNumberPicker(
                        value = value,
                        onValueChange = onValueChange,
                        range = range,
                        label = label
                    )
                }
            }
        },
        dismissButton = {
            Button(
                onClick = onDismiss,
                modifier = Modifier.height(32.dp),
                colors = ButtonDefaults.buttonColors()
                    .copy(containerColor = MaterialTheme.colorScheme.surfaceBright),
                shape = RoundedCornerShape(32.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp)
            ) {
                Text("Отмена", fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)
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
                Text("Сохранить", fontSize = 16.sp, color = MaterialTheme.colorScheme.onPrimary)
            }
        }
    )
}

/* -------------------- WheelNumberPicker -------------------- */

@Composable
private fun WheelNumberPicker(
    value: Int,
    onValueChange: (Int) -> Unit,
    range: IntRange,
    modifier: Modifier = Modifier,
    visibleItemsCount: Int = 5,
    itemHeight: Dp = 44.dp,
    label: String? = null
) {
    require(visibleItemsCount % 2 == 1) { "visibleItemsCount must be odd" }

    val values = remember(range) { range.toList() }
    val centerOffset = visibleItemsCount / 2

    val listState = rememberLazyListState()
    val flingBehavior = rememberSnapFlingBehavior(lazyListState = listState)

    val updatedOnValueChange by rememberUpdatedState(onValueChange)

    fun targetIndexFor(v: Int): Int =
        (v - range.first).coerceIn(0, values.lastIndex)

    var didInitialScroll by remember { mutableStateOf(false) }

    LaunchedEffect(range.first, range.last, visibleItemsCount, value) {
        if (didInitialScroll) return@LaunchedEffect
        didInitialScroll = true
        val target = targetIndexFor(value)
        val targetLazy = target + 1
        val firstVisible = (targetLazy - centerOffset).coerceAtLeast(0)
        listState.scrollToItem(firstVisible)
    }

    LaunchedEffect(listState, values) {
        snapshotFlow { listState.centerValueIndex(valuesCount = values.size) }
            .distinctUntilChanged()
            .collect { idx ->
                val newValue = values[idx]
                if (newValue != value) updatedOnValueChange(newValue)
            }
    }

    LaunchedEffect(value) {
        snapshotFlow { listState.isScrollInProgress }
            .map { inProgress -> !inProgress }
            .filter { it }
            .distinctUntilChanged()
            .collect {
                val target = targetIndexFor(value)
                val current = listState.centerValueIndex(valuesCount = values.size)
                if (current != target) {
                    val targetLazy = target + 1
                    val firstVisible = (targetLazy - centerOffset).coerceAtLeast(0)
                    listState.animateScrollToItem(firstVisible)
                }
            }
    }

    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        if (label != null) {
            Text(label, style = MaterialTheme.typography.bodyMedium)
            Spacer(Modifier.height(8.dp))
        }

        val pickerHeight = itemHeight * visibleItemsCount

        Box(
            modifier = Modifier
                .height(pickerHeight)
                .width(120.dp),
            contentAlignment = Alignment.Center
        ) {
            LazyColumn(
                state = listState,
                flingBehavior = flingBehavior,
                modifier = Modifier.fillMaxSize()
            ) {
                item { Spacer(Modifier.height(itemHeight * centerOffset)) }

                itemsIndexed(values) { index, v ->
                    val selectedIndex = listState.centerValueIndex(valuesCount = values.size)
                    val isSelected = index == selectedIndex

                    Box(
                        modifier = Modifier
                            .height(itemHeight)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        val distance = abs(index - selectedIndex).coerceAtMost(centerOffset + 2)
                        val a = when (distance) {
                            0 -> 1f
                            1 -> 0.55f
                            2 -> 0.35f
                            else -> 0.22f
                        }

                        Text(
                            text = v.toString(),
                            fontSize = if (isSelected) 22.sp else 18.sp,
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.alpha(a)
                        )
                    }
                }

                item { Spacer(Modifier.height(itemHeight * centerOffset)) }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(itemHeight)
                    .align(Alignment.Center)
                    .background(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f),
                        shape = RoundedCornerShape(14.dp)
                    )
            )

            val fadeColor = MaterialTheme.colorScheme.surface
            Column(Modifier.fillMaxSize()) {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .background(
                            Brush.verticalGradient(
                                0f to fadeColor,
                                1f to Color.Transparent
                            )
                        )
                )
                Box(
                    Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .background(
                            Brush.verticalGradient(
                                0f to Color.Transparent,
                                1f to fadeColor
                            )
                        )
                )
            }
        }
    }
}

/* -------------------- centerValueIndex helper -------------------- */

private fun LazyListState.centerValueIndex(valuesCount: Int): Int {
    val layout = layoutInfo
    val viewportCenter = (layout.viewportStartOffset + layout.viewportEndOffset) / 2
    val visible = layout.visibleItemsInfo
    if (visible.isEmpty()) return 0

    val closest = visible.minByOrNull { item ->
        val itemCenter = item.offset + item.size / 2
        abs(itemCenter - viewportCenter)
    } ?: return 0

    val lazyIndex = closest.index
    return (lazyIndex - 1).coerceIn(0, valuesCount - 1)
}

