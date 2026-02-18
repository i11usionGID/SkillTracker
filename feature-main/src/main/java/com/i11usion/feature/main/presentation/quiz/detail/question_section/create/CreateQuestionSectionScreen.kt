package com.i11usion.feature.main.presentation.quiz.detail.question_section.create


import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
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
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.i11usion.core.ui.di.LocalViewModelFactory
import com.i11usion.feature.main.presentation.quiz.detail.question_section.create.model.QuestionInQuestionSectionUiModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlin.math.abs

@Composable
fun CreateQuestionSectionScreen(
    modifier: Modifier = Modifier,
    quizId: Int,
    onCreateQuestionsClick: () -> Unit,
    onCancelClick: () -> Unit
) {
    val viewModel: CreateQuestionSectionViewModel =
        viewModel(factory = LocalViewModelFactory.current)

    val state by viewModel.state.collectAsState()

    LaunchedEffect(quizId) {
        viewModel.processCommand(CreateQuestionSectionCommand.Init(quizId))
    }

    LaunchedEffect(viewModel) {
        viewModel.events.collectLatest { event ->
            when (event) {
                CreateQuestionSectionEvent.Close -> onCancelClick()
                CreateQuestionSectionEvent.Created -> onCancelClick()
                is CreateQuestionSectionEvent.ShowError -> {
                    // если у тебя есть snackbar-хост — покажешь там.
                    // пока можно оставить пустым или подключить по месту
                }
            }
        }
    }

    when (val s = state) {
        CreateQuestionSectionUiState.Initial -> Unit

        CreateQuestionSectionUiState.Loading -> Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) { CircularProgressIndicator() }

        is CreateQuestionSectionUiState.Error -> Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) { Text(s.message) }

        is CreateQuestionSectionUiState.Content -> CreateQuestionSectionContent(
            modifier = modifier,
            state = s,
            onCommand = viewModel::processCommand,
            onCreateQuestionsClick = onCreateQuestionsClick
        )
    }
}

@Composable
private fun CreateQuestionSectionContent(
    modifier: Modifier = Modifier,
    state: CreateQuestionSectionUiState.Content,
    onCommand: (CreateQuestionSectionCommand) -> Unit,
    onCreateQuestionsClick: () -> Unit
) {
    var showOrderDialog by remember { mutableStateOf(false) }
    var showPickDialog by remember { mutableStateOf(false) }

    var tempOrder by remember(state.orderIndex) {
        mutableIntStateOf(
            state.orderIndex.coerceIn(
                1,
                100
            )
        )
    }
    var tempPick by remember(state.pickCount) { mutableIntStateOf(state.pickCount ?: 1) }

    val selectedCount = state.selectedCount
    val hasSelected = selectedCount > 0

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        item {
            Spacer(Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Spacer(Modifier.weight(1f))
                Text(
                    text = "Создать секцию",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.weight(1f))
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = null,
                    modifier = Modifier
                        .size(28.dp)
                        .clickable { onCommand(CreateQuestionSectionCommand.CloseClicked) },
                    tint = MaterialTheme.colorScheme.surfaceVariant
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
                        "Название секции",
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 16.sp
                    )
                    Spacer(Modifier.height(8.dp))
                    InputField(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 56.dp),
                        query = state.titleQuery,
                        placeHolderText = "Введите название секции",
                        onQueryChanged = { onCommand(CreateQuestionSectionCommand.ChangeTitle(it)) }
                    )

                    Spacer(Modifier.height(16.dp))

                    Text("Описание", color = MaterialTheme.colorScheme.onSurface, fontSize = 16.sp)
                    Spacer(Modifier.height(8.dp))
                    InputField(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 120.dp),
                        query = state.descriptionQuery,
                        placeHolderText = "Описание (необязательно)",
                        onQueryChanged = {
                            onCommand(
                                CreateQuestionSectionCommand.ChangeDescription(
                                    it
                                )
                            )
                        }
                    )

                    Spacer(Modifier.height(16.dp))

                    Text("Порядок", color = MaterialTheme.colorScheme.onSurface, fontSize = 16.sp)
                    Spacer(Modifier.height(8.dp))
                    ClickField(
                        text = state.orderIndex.toString(),
                        placeHolderText = "Выберите порядок",
                        isFocused = false,
                        enabled = !state.isCreating, // по твоему ограничению: лучше завязать на выбранные вопросы
                        onClickFieldClick = {
                            tempOrder = state.orderIndex.coerceIn(1, 100)
                            showOrderDialog = true
                        }
                    )

                    Spacer(Modifier.height(16.dp))

                    Text(
                        "Выбрать вопросов (оставьте пустым для всех)",
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 16.sp
                    )
                    Spacer(Modifier.height(8.dp))
                    ClickField(
                        text = state.pickCount?.toString().orEmpty(),
                        placeHolderText = if (!hasSelected) "Сначала выберите вопросы ниже" else "Пусто = все выбранные",
                        isFocused = false,
                        enabled = hasSelected,
                        onClickFieldClick = {
                            tempPick =
                                (state.pickCount ?: 1).coerceIn(1, selectedCount.coerceAtLeast(1))
                            showPickDialog = true
                        }
                    )

                    Spacer(Modifier.height(16.dp))

                    Text(
                        "Выберите вопросы",
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 16.sp
                    )
                    Spacer(Modifier.height(8.dp))

                    QuestionsPickerList(
                        questions = state.questions,
                        selectedIds = state.selectedQuestionIds,
                        enabled = !state.isCreating,
                        onToggle = { id, checked ->
                            onCommand(CreateQuestionSectionCommand.ToggleQuestion(id, checked))
                        },
                        onCreateQuestionsClick = onCreateQuestionsClick
                    )

                    Spacer(Modifier.height(16.dp))

                    Row(Modifier.fillMaxWidth()) {
                        Button(
                            modifier = Modifier
                                .weight(1f)
                                .height(64.dp),
                            onClick = { onCommand(CreateQuestionSectionCommand.CloseClicked) },
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
                            onClick = { onCommand(CreateQuestionSectionCommand.CreateClicked) },
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

            Spacer(Modifier.height(16.dp))
        }
    }

    // Диалог "Порядок" -> 1..selectedCount
    if (showOrderDialog) {
        NumberWheelDialog(
            title = "Порядок",
            value = tempOrder.coerceIn(1, 100),
            range = 1..100,
            label = "Порядок",
            onValueChange = { tempOrder = it },
            onDismiss = { showOrderDialog = false },
            onSaveClick = {
                showOrderDialog = false
                onCommand(CreateQuestionSectionCommand.ChangeOrderIndex(tempOrder))
            }
        )
    }

    // Диалог "pickCount" -> null или 1..selectedCount
    if (showPickDialog) {
        val max = selectedCount.coerceAtLeast(1)
        PickCountDialog(
            selectedCount = selectedCount,
            current = state.pickCount,
            tempValue = tempPick.coerceIn(1, max),
            onTempChange = { tempPick = it.coerceIn(1, max) },
            onDismiss = { showPickDialog = false },
            onSaveAll = {
                showPickDialog = false
                onCommand(CreateQuestionSectionCommand.ChangePickCount(null)) // пусто = все
            },
            onSaveNumber = {
                showPickDialog = false
                onCommand(CreateQuestionSectionCommand.ChangePickCount(tempPick.coerceIn(1, max)))
            }
        )
    }
}

@Composable
private fun QuestionsPickerList(
    questions: List<QuestionInQuestionSectionUiModel>,
    selectedIds: Set<Int>,
    enabled: Boolean,
    onToggle: (id: Int, checked: Boolean) -> Unit,
    onCreateQuestionsClick: () -> Unit, // ✅ добавили
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.onPrimary),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceBright.copy(alpha = 0.3f))
    ) {
        if (questions.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Нет доступных вопросов. ",
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        style = MaterialTheme.typography.bodyMedium
                    )

                    Text(
                        text = "Создайте вопросы",
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.clickable { onCreateQuestionsClick() }
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 420.dp),
                contentPadding = PaddingValues(8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(items = questions, key = { it.id }) { q ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .clickable(enabled = enabled) {
                                val now = selectedIds.contains(q.id)
                                onToggle(q.id, !now)
                            }
                            .padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = selectedIds.contains(q.id),
                            onCheckedChange = { checked -> onToggle(q.id, checked) },
                            enabled = enabled
                        )
                        Spacer(Modifier.width(8.dp))
                        Column(Modifier.weight(1f)) {
                            Text(
                                text = q.text,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontSize = 15.sp,
                                maxLines = 2
                            )
                            Spacer(Modifier.height(2.dp))
                            Text(
                                text = q.text,
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

/* -------------------- поля (как в CreateQuizScreen) -------------------- */

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
                    placeHolderText,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.surfaceBright
                )
            }
        },
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        interactionSource = interactionSource,
        colors = OutlinedTextFieldDefaults.colors(
            unfocusedBorderColor = MaterialTheme.colorScheme.surfaceBright,
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedPlaceholderColor = MaterialTheme.colorScheme.surfaceBright,
        )
    )
}

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

/* -------------------- Диалоги WheelPicker -------------------- */

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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(title, style = MaterialTheme.typography.titleLarge)
                IconButton(onClick = onDismiss) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = null
                    )
                }
            }
        },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    value.toString(),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.height(12.dp))
                WheelNumberPicker(
                    value = value,
                    onValueChange = onValueChange,
                    range = range,
                    label = label
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
                Text("Сохранить", fontSize = 16.sp, color = MaterialTheme.colorScheme.onPrimary)
            }
        }
    )
}

/**
 * Диалог pickCount:
 * - "Оставить пустым (все)" -> pickCount = null
 * - "Сохранить число" -> pickCount = tempPick
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PickCountDialog(
    selectedCount: Int,
    current: Int?,
    tempValue: Int,
    onTempChange: (Int) -> Unit,
    onDismiss: () -> Unit,
    onSaveAll: () -> Unit,
    onSaveNumber: () -> Unit
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
                Text("Выбрать вопросов", style = MaterialTheme.typography.titleLarge)
                IconButton(onClick = onDismiss) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = null
                    )
                }
            }
        },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Выбрано вопросов: $selectedCount",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(Modifier.height(12.dp))

                Text(
                    text = "Текущее: ${current?.toString() ?: "пусто (все)"}",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(Modifier.height(12.dp))

                WheelNumberPicker(
                    value = tempValue,
                    onValueChange = onTempChange,
                    range = 1..selectedCount.coerceAtLeast(1),
                    label = "Количество"
                )

                Spacer(Modifier.height(12.dp))

                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        modifier = Modifier.weight(1f),
                        onClick = onSaveAll,
                        colors = ButtonDefaults.buttonColors()
                            .copy(containerColor = MaterialTheme.colorScheme.surfaceBright),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text("Пусто = все", color = MaterialTheme.colorScheme.onSurface)
                    }

                    Button(
                        modifier = Modifier.weight(1f),
                        onClick = onSaveNumber,
                        colors = ButtonDefaults.buttonColors()
                            .copy(containerColor = MaterialTheme.colorScheme.primary),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text("Сохранить", color = MaterialTheme.colorScheme.onPrimary)
                    }
                }
            }
        },
        confirmButton = {}
    )
}

/* -------- WheelNumberPicker (скопирован из твоего примера) -------- */

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
