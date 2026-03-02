package com.i11usion.feature.main.presentation.quiz.create

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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.i11usion.core.ui.di.LocalViewModelFactory
import com.i11usion.core.ui.helpers.asString
import com.i11usion.core.ui.template.ErrorScreen
import com.i11usion.feature.main.R
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlin.math.abs

@Composable
fun CreateQuizScreen(
    modifier: Modifier = Modifier,
    onCancelClick: () -> Unit
) {
    val viewModel: CreateQuizViewModel = viewModel(factory = LocalViewModelFactory.current)
    val uiState = viewModel.state.collectAsState()
    val state = uiState.value

    val context = LocalContext.current

    LaunchedEffect(viewModel) {
        viewModel.events.collectLatest { event ->
            when (event) {
                CreateQuizEvent.Close -> onCancelClick()
                CreateQuizEvent.Created -> {
                    onCancelClick()
                }

                is CreateQuizEvent.ShowError -> {
                }
            }
        }
    }

    when (state) {
        CreateQuizUiState.Initial -> {}

        is CreateQuizUiState.Content -> {
            var showPassDialog by remember { mutableStateOf(false) }
            var showTimeDialog by remember { mutableStateOf(false) }
            var showAttemptsDialog by remember { mutableStateOf(false) }

            val maxTimeSeconds = 23 * 3600 + 59 * 60 + 59
            val timeLimit = (state.timeLimitSeconds ?: 0).coerceIn(0, maxTimeSeconds)

            val attempts = state.maxAttempts ?: 1

            var tempPassPercent by remember(state.pasPercent) { mutableIntStateOf(state.pasPercent) }

            val initialHours = timeLimit / 3600
            val initialMinutes = (timeLimit % 3600) / 60
            val initialSeconds = timeLimit % 60

            var tempHours by remember(timeLimit) { mutableIntStateOf(initialHours) }
            var tempMinutes by remember(timeLimit) { mutableIntStateOf(initialMinutes) }
            var tempSeconds by remember(timeLimit) { mutableIntStateOf(initialSeconds) }

            var tempAttempts by remember(attempts) { mutableIntStateOf(attempts.coerceIn(1, 10)) }

            val fsError = state.fullScreenError
            if (fsError != null) {
                ErrorScreen(
                    modifier = modifier.fillMaxSize(),
                    iconResId = fsError.iconResId,
                    cdResId = fsError.cdResId,
                    errorText = fsError.text.asString(LocalContext.current),
                    onRefreshClick = { viewModel.processCommand(CreateQuizCommand.Refresh) }
                )
                return
            }

            LazyColumn(
                modifier = modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
            ) {
                item {
                    Spacer(modifier = Modifier.height(16.dp))

                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = stringResource(R.string.create_quiz),
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }

                item {
                    CreateQuizCard(
                        titleQuery = state.titleQuery,
                        descriptionQuery = state.descriptionQuery.orEmpty(),
                        passPercent = state.pasPercent,
                        timeLimitSeconds = state.timeLimitSeconds,
                        maxAttempts = state.maxAttempts,
                        publish = state.publish,
                        shuffleAnswers = state.shuffleAnswers,
                        shuffleQuestions = state.shuffleQuestions,
                        allowReview = state.allowReview,

                        canCreate = state.canCreate,
                        isCreating = state.isCreating,

                        onTitleQueryChanged = {
                            viewModel.processCommand(
                                CreateQuizCommand.ChangeTitle(
                                    it
                                )
                            )
                        },
                        onDescriptionQueryChanged = {
                            viewModel.processCommand(
                                CreateQuizCommand.ChangeDescription(
                                    it
                                )
                            )
                        },

                        onPassPercentClick = {
                            tempPassPercent = state.pasPercent
                            showPassDialog = true
                        },
                        onTimeLimitSecondsClick = {
                            tempHours = initialHours
                            tempMinutes = initialMinutes
                            tempSeconds = initialSeconds
                            showTimeDialog = true
                        },
                        onMaxAttemptsClick = {
                            tempAttempts = (state.maxAttempts ?: 1).coerceIn(1, 10)
                            showAttemptsDialog = true
                        },

                        onTogglePublishClick = { viewModel.processCommand(CreateQuizCommand.TogglePublish) },
                        onToggleShuffleAnswersClick = { viewModel.processCommand(CreateQuizCommand.ToggleShuffleAnswers) },
                        onToggleShuffleQuestionsClick = { viewModel.processCommand(CreateQuizCommand.ToggleShuffleQuestions) },
                        onToggleAllowReviewClick = { viewModel.processCommand(CreateQuizCommand.ToggleAllowReview) },

                        onCancelClick = {
                            viewModel.processCommand(CreateQuizCommand.Cancel)
                        },
                        onCreateQuizClick = { viewModel.processCommand(CreateQuizCommand.Create) }
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                }
            }

            if (showPassDialog) {
                UpdatePassPercentDialog(
                    progress = tempPassPercent,
                    onProgressChange = { tempPassPercent = it.coerceIn(0, 100) },
                    onDismiss = { showPassDialog = false },
                    onSaveClick = {
                        showPassDialog = false
                        viewModel.processCommand(CreateQuizCommand.ChangePassPercent(tempPassPercent))
                    }
                )
            }

            if (showTimeDialog) {
                TimeLimitDialog(
                    hours = tempHours,
                    minutes = tempMinutes,
                    seconds = tempSeconds,
                    onHoursChange = { tempHours = it.coerceIn(0, 23) },
                    onMinutesChange = { tempMinutes = it.coerceIn(0, 59) },
                    onSecondsChange = { tempSeconds = it.coerceIn(0, 59) },
                    onDismiss = { showTimeDialog = false },
                    onSaveClick = {
                        showTimeDialog = false
                        val totalSeconds = tempHours * 3600 + tempMinutes * 60 + tempSeconds
                        viewModel.processCommand(
                            CreateQuizCommand.ChangeTimeLimitSeconds(
                                totalSeconds
                            )
                        )
                    }
                )
            }

            if (showAttemptsDialog) {
                MaxAttemptsDialog(
                    attempts = tempAttempts,
                    onAttemptsChange = { tempAttempts = it.coerceIn(1, 10) },
                    onDismiss = { showAttemptsDialog = false },
                    onSaveClick = {
                        showAttemptsDialog = false
                        viewModel.processCommand(CreateQuizCommand.ChangeMaxAttempts(tempAttempts))
                    }
                )
            }
        }

        is CreateQuizUiState.Error -> {
            ErrorScreen(
                modifier = modifier.fillMaxSize(),
                iconResId = state.model.iconResId,
                cdResId = state.model.cdResId,
                errorText = state.model.text.asString(context),
                onRefreshClick = { viewModel.processCommand(CreateQuizCommand.Refresh) }
            )
        }
    }
}

@Composable
private fun CreateQuizCard(
    modifier: Modifier = Modifier,
    titleQuery: String,
    descriptionQuery: String,
    passPercent: Int,
    timeLimitSeconds: Int?,
    maxAttempts: Int?,
    publish: Boolean,
    shuffleAnswers: Boolean,
    shuffleQuestions: Boolean,
    allowReview: Boolean,
    canCreate: Boolean,
    isCreating: Boolean,
    onTitleQueryChanged: (String) -> Unit,
    onDescriptionQueryChanged: (String) -> Unit,
    onPassPercentClick: () -> Unit,
    onTimeLimitSecondsClick: () -> Unit,
    onMaxAttemptsClick: () -> Unit,
    onTogglePublishClick: () -> Unit,
    onToggleShuffleAnswersClick: () -> Unit,
    onToggleShuffleQuestionsClick: () -> Unit,
    onToggleAllowReviewClick: () -> Unit,
    onCancelClick: () -> Unit,
    onCreateQuizClick: () -> Unit
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
                text = stringResource(R.string.title_name),
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 16.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            InputField(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 56.dp),
                query = titleQuery,
                placeHolderText = stringResource(R.string.enter_quiz_name),
                onQueryChanged = onTitleQueryChanged
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = stringResource(R.string.description_not_required),
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 16.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            InputField(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 120.dp),
                query = descriptionQuery,
                placeHolderText = stringResource(R.string.describe_test),
                onQueryChanged = onDescriptionQueryChanged
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = stringResource(R.string.passing_score_percent),
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 16.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            ClickField(
                text = passPercent.toString(),
                placeHolderText = stringResource(R.string.pass_percent_for_successful_complete_quiz),
                isFocused = false,
                onClickFieldClick = onPassPercentClick,
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = stringResource(R.string.time_limit_not_required),
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 16.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            ClickField(
                text = timeLimitSeconds?.let { formatHms(it) }.orEmpty(),
                placeHolderText = stringResource(R.string.time_limit_for_completing_test),
                isFocused = false,
                onClickFieldClick = onTimeLimitSecondsClick,
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = stringResource(R.string.max_attempts_not_required),
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 16.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            ClickField(
                text = maxAttempts?.toString().orEmpty(),
                placeHolderText = stringResource(R.string.max_attempts_test_hit),
                isFocused = false,
                onClickFieldClick = onMaxAttemptsClick,
            )

            Spacer(modifier = Modifier.height(16.dp))

            CheckField(
                checked = publish,
                title = stringResource(R.string.publish_immediately),
                onToggle = onTogglePublishClick
            )

            Spacer(modifier = Modifier.height(8.dp))

            CheckField(
                checked = shuffleAnswers,
                title = stringResource(R.string.shiffle_questions),
                onToggle = onToggleShuffleAnswersClick
            )

            Spacer(modifier = Modifier.height(8.dp))

            CheckField(
                checked = shuffleQuestions,
                title = stringResource(R.string.shuffle_answers),
                onToggle = onToggleShuffleQuestionsClick
            )

            Spacer(modifier = Modifier.height(8.dp))

            CheckField(
                checked = allowReview,
                title = stringResource(R.string.allow_review),
                onToggle = onToggleAllowReviewClick
            )

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
                    onClick = onCreateQuizClick,
                    enabled = canCreate && !isCreating,
                    colors = ButtonDefaults.buttonColors()
                        .copy(
                            containerColor = MaterialTheme.colorScheme.primary,
                            disabledContainerColor = MaterialTheme.colorScheme.primary.copy(
                                alpha = 0.3f
                            ),
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

            Spacer(modifier = Modifier.height(16.dp))
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
    onClickFieldClick: () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp)
            .clip(shape = RoundedCornerShape(20.dp))
            .border(
                width = 1.dp, shape = RoundedCornerShape(20.dp), color = if (isFocused) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.surfaceBright
                }
            )
            .clickable(onClick = onClickFieldClick),
        contentAlignment = Alignment.TopStart
    ) {
        Text(
            text = text.ifBlank {
                placeHolderText
            },
            modifier = Modifier.padding(16.dp),
            fontSize = 16.sp,
            color = if (text.isBlank()) {
                MaterialTheme.colorScheme.surfaceBright
            } else {
                MaterialTheme.colorScheme.onSurface
            }
        )
    }
}

@Composable
private fun CheckField(
    modifier: Modifier = Modifier,
    checked: Boolean,
    title: String,
    onToggle: () -> Unit
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = { onToggle() }
        )

        Spacer(modifier = Modifier.width(8.dp))

        Text(
            text = title,
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun UpdatePassPercentDialog(
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
                    text = stringResource(R.string.set_pass_percent),
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
                    text = stringResource(R.string.pass_percent) + ": $progress%",
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TimeLimitDialog(
    hours: Int,
    minutes: Int,
    seconds: Int,
    onHoursChange: (Int) -> Unit,
    onMinutesChange: (Int) -> Unit,
    onSecondsChange: (Int) -> Unit,
    onDismiss: () -> Unit,
    onSaveClick: () -> Unit,
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
                    text = stringResource(R.string.time_limit_for_completing_test),
                    style = MaterialTheme.typography.titleLarge
                )
                IconButton(onClick = onDismiss) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = stringResource(R.string.cd_close)
                    )
                }
            }
        },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "%02d:%02d:%02d".format(hours, minutes, seconds),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    WheelNumberPicker(
                        value = hours,
                        onValueChange = onHoursChange,
                        range = 0..23,
                        label = "Часы"
                    )
                    WheelNumberPicker(
                        value = minutes,
                        onValueChange = onMinutesChange,
                        range = 0..59,
                        label = "Мин"
                    )
                    WheelNumberPicker(
                        value = seconds,
                        onValueChange = onSecondsChange,
                        range = 0..59,
                        label = "Сек"
                    )
                }
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MaxAttemptsDialog(
    attempts: Int,
    onAttemptsChange: (Int) -> Unit,
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
                    text = stringResource(R.string.max_attempts_not_required),
                    style = MaterialTheme.typography.titleLarge
                )
                IconButton(onClick = onDismiss) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = stringResource(R.string.cd_close)
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = attempts.toString(),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.height(12.dp))

                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    WheelNumberPicker(
                        value = attempts,
                        onValueChange = { onAttemptsChange(it.coerceIn(1, 10)) },
                        range = 1..10,
                        label = "Попытки"
                    )
                }
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
        val targetLazy =
            target + 1
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
                .width(96.dp),
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
                            text = v.toString().padStart(2, '0'),
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


private fun formatHms(totalSeconds: Int): String {
    val s = totalSeconds.coerceAtLeast(0)
    val h = s / 3600
    val m = (s % 3600) / 60
    val sec = s % 60
    return "%02d:%02d:%02d".format(h, m, sec)
}

