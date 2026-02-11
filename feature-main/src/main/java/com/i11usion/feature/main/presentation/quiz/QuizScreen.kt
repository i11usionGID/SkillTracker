package com.i11usion.feature.main.presentation.quiz

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.i11usion.core.domain.model.quiz.QuizStatus
import com.i11usion.core.domain.model.user.Role
import com.i11usion.core.ui.di.LocalViewModelFactory
import com.i11usion.feature.main.R

@Composable
fun QuizScreen(
    modifier: Modifier = Modifier,
    onCreateTestClick: () -> Unit,
    onOpenQuestionListClick: () -> Unit,
    onOpenQuizClick: (String) -> Unit,
    onStartTestingClick: (String) -> Unit
) {
    val viewModel: QuizViewModel = viewModel(factory = LocalViewModelFactory.current)
    val uiState = viewModel.state.collectAsState()
    val state = uiState.value

    when (state) {
        QuizUiState.Initial -> {}
        QuizUiState.Loading -> {
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

        is QuizUiState.Content -> {
            LazyColumn(
                modifier = modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
            ) {
                item {
                    QuizHeader(
                        userRole = state.user.role,
                        onCreateTestClick = onCreateTestClick,
                        onOpenQuestionListClick = onOpenQuestionListClick
                    )
                }

                if (state.user.role == Role.MANAGER) {
                    item {
                        FilterCard(
                            query = state.quizFilters.query,
                            selectedStatus = state.quizFilters.status,
                            onQueryChanged = {
                                viewModel.processCommand(
                                    QuizCommand.InputSearchQuery(it)
                                )
                            },
                            onStatusChanged = {
                                viewModel.processCommand(
                                    QuizCommand.SelectStatus(it)
                                )
                            },
                        )

                        Spacer(modifier = Modifier.height(32.dp))
                    }
                }

                if (state.isListLoading) {
                    item {
                        Spacer(modifier = Modifier.height(64.dp))
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
                    when (state.user.role) {
                        Role.MANAGER -> {
                            if (state.quizList.isEmpty()) {
                                item {
                                    Spacer(modifier = Modifier.height(32.dp))
                                    QuizNotFound(
                                        iconCdResId = R.string.cd_no_quiz_yet,
                                        titleResId = R.string.quizzes_not_found,
                                        descriptionResId = R.string.no_quiz_yet
                                    )
                                }
                            } else {
                                items(
                                    items = state.quizList,
                                    key = { quiz -> quiz.id }
                                ) { quiz ->
                                    val statusColor: Color
                                    val statusContainerColor: Color
                                    when (quiz.status) {
                                        QuizStatus.DRAFT -> {
                                            statusColor = MaterialTheme.colorScheme.surfaceVariant
                                            statusContainerColor =
                                                MaterialTheme.colorScheme.surfaceBright
                                        }

                                        QuizStatus.PUBLISHED -> {
                                            statusColor = MaterialTheme.colorScheme.tertiary
                                            statusContainerColor =
                                                MaterialTheme.colorScheme.tertiaryContainer
                                        }
                                    }
                                    QuizCardManager(
                                        title = quiz.title,
                                        description = quiz.description,
                                        passPercent = quiz.passPercent,
                                        timeLimitSeconds = quiz.timeLimitSeconds,
                                        maxAttempts = quiz.maxAttempts,
                                        statusText = quiz.statusText,
                                        statusColor = statusColor,
                                        statusContainerColor = statusContainerColor,
                                        createdAt = quiz.createdAt,
                                        onOpenQuizClick = { onOpenQuizClick(quiz.id.toString()) },
                                        onDeleteQuizClick = {
                                            viewModel.processCommand(
                                                QuizCommand.DeleteQuiz(
                                                    quiz.id
                                                )
                                            )
                                        }
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))
                                }
                            }
                        }

                        Role.EMPLOYEE -> {
                            if (state.assignmentsList.isEmpty()) {
                                item {
                                    Spacer(modifier = Modifier.height(32.dp))
                                    QuizNotFound(
                                        iconCdResId = R.string.cd_no_quiz_for_employee,
                                        titleResId = R.string.no_quiz_for_employee,
                                        descriptionResId = R.string.you_have_not_test_yet
                                    )
                                }
                            } else {
                                items(
                                    items = state.assignmentsList,
                                    key = { quiz -> quiz.id }
                                ) { quiz ->
                                    QuizCardEmployee(
                                        title = quiz.title,
                                        description = quiz.description,
                                        required = quiz.required,
                                        passPercent = quiz.passPercent,
                                        timeLimitSeconds = quiz.timeLimitSeconds,
                                        attempts = quiz.attempts,
                                        deadline = quiz.deadline,
                                        isFirstAttempt = quiz.isFirstAttempt,
                                        isNoAttempts = quiz.isNoAttempts,
                                        onStartTestingClick = { onStartTestingClick(quiz.id.toString()) },
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))
                                }
                            }
                        }
                    }
                }
            }
        }

        is QuizUiState.Error -> {
            Text(state.message)
        }
    }
}

@Composable
private fun QuizHeader(
    modifier: Modifier = Modifier,
    userRole: Role,
    onCreateTestClick: () -> Unit,
    onOpenQuestionListClick: () -> Unit
) {
    Spacer(modifier = Modifier.height(16.dp))

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = if (userRole == Role.MANAGER) {
                stringResource(R.string.tests)
            } else {
                stringResource(R.string.my_tests)
            },
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = if (userRole == Role.MANAGER) {
                stringResource(R.string.manage_tests_and_questions)
            } else {
                stringResource(R.string.test_prescribed_for_you)
            },
            textAlign = TextAlign.Center,
            fontSize = 18.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        if (userRole == Role.MANAGER) {

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors()
                    .copy(containerColor = MaterialTheme.colorScheme.primary),
                shape = RoundedCornerShape(16.dp),
                onClick = onCreateTestClick
            ) {
                Text(
                    text = stringResource(R.string.create_test),
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors()
                    .copy(containerColor = MaterialTheme.colorScheme.surfaceBright),
                shape = RoundedCornerShape(16.dp),
                onClick = onOpenQuestionListClick
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_circle_question_mark),
                        contentDescription = stringResource(R.string.question_list),
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.question_list),
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }

    Spacer(modifier = Modifier.height(16.dp))
}

@Composable
private fun FilterCard(
    modifier: Modifier = Modifier,
    query: String,
    selectedStatus: QuizStatus?,
    onQueryChanged: (String) -> Unit,
    onStatusChanged: (QuizStatus?) -> Unit
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

            val roleOptions = statusOptions()
            val selectedRoleTitle =
                roleOptions.firstOrNull { it.value == selectedStatus }?.title
                    ?: roleOptions.first().title

            DropdownField(
                options = roleOptions.map { it.title },
                selected = selectedRoleTitle,
                onSelectedChange = { title ->
                    val role = roleOptions.first { it.title == title }.value
                    onStatusChanged(role)
                }
            )
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
                contentDescription = stringResource(R.string.cd_search_text_task_or_quiz),
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
private fun QuizCardManager(
    modifier: Modifier = Modifier,
    title: String,
    description: String?,
    passPercent: String,
    timeLimitSeconds: String?,
    maxAttempts: String?,
    statusText: String,
    statusColor: Color,
    statusContainerColor: Color,
    createdAt: String,
    onOpenQuizClick: () -> Unit,
    onDeleteQuizClick: () -> Unit
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
        Column(modifier = Modifier.padding(vertical = 24.dp, horizontal = 24.dp)) {

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
                        fontSize = 14.sp
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = passPercent,
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 14.sp,
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

            Spacer(modifier = Modifier.height(16.dp))

            if (!description.isNullOrBlank()) {
                Text(
                    text = description,
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    fontSize = 16.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (!timeLimitSeconds.isNullOrBlank()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        painter = painterResource(R.drawable.ic_watch),
                        contentDescription = stringResource(R.string.cd_time_limit_seconds_on_quiz),
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.surfaceVariant
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = timeLimitSeconds,
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        fontSize = 16.sp
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            if (!maxAttempts.isNullOrBlank()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        painter = painterResource(com.i11usion.core.ui.R.drawable.ic_logo),
                        contentDescription = stringResource(R.string.cd_max_attempts_on_quiz),
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.surfaceVariant
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = maxAttempts,
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        fontSize = 16.sp
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            Text(
                text = stringResource(R.string.created_at) + createdAt,
                color = MaterialTheme.colorScheme.surfaceVariant,
                fontSize = 16.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row {
                Button(
                    modifier = Modifier
                        .weight(1f),
                    colors = ButtonDefaults.buttonColors()
                        .copy(containerColor = MaterialTheme.colorScheme.surfaceBright),
                    shape = RoundedCornerShape(32.dp),
                    onClick = onOpenQuizClick
                ) {
                    Text(
                        text = stringResource(R.string.edit),
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    modifier = Modifier,
                    colors = ButtonDefaults.buttonColors()
                        .copy(containerColor = MaterialTheme.colorScheme.onError),
                    shape = RoundedCornerShape(32.dp),
                    onClick = onDeleteQuizClick
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_trash),
                        contentDescription = stringResource(R.string.cd_delete_test),
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        }
    }
}

@Composable
private fun QuizCardEmployee(
    modifier: Modifier = Modifier,
    title: String,
    description: String?,
    required: Boolean,
    passPercent: String,
    timeLimitSeconds: String?,
    attempts: String?,
    deadline: String?,
    isFirstAttempt: Boolean?,
    isNoAttempts: Boolean,
    onStartTestingClick: () -> Unit
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
        Column(modifier = Modifier.padding(vertical = 24.dp, horizontal = 24.dp)) {

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold
                )
                if (required) {
                    Spacer(modifier.weight(1f))

                    Icon(
                        painterResource(R.drawable.ic_required),
                        contentDescription = stringResource(R.string.cd_required_quiz),
                        modifier = Modifier.size(32.dp),
                        tint = MaterialTheme.colorScheme.onError
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (!description.isNullOrBlank()) {
                Text(
                    text = description,
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    fontSize = 16.sp
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            IconWithText(
                iconResId = com.i11usion.core.ui.R.drawable.ic_logo,
                contentDescriptionResId = R.string.cd_pass_percent,
                text = passPercent,
                color = MaterialTheme.colorScheme.surfaceVariant
            )

            if (!timeLimitSeconds.isNullOrBlank()) {
                IconWithText(
                    iconResId = R.drawable.ic_watch,
                    contentDescriptionResId = R.string.cd_time_limit_seconds_on_quiz,
                    text = timeLimitSeconds,
                    color = MaterialTheme.colorScheme.surfaceVariant
                )
            }

            if (!attempts.isNullOrBlank()) {
                IconWithText(
                    iconResId = R.drawable.ic_refresh,
                    contentDescriptionResId = R.string.cd_quiz_attempts,
                    text = attempts,
                    color = MaterialTheme.colorScheme.surfaceVariant
                )
            }

            if (!deadline.isNullOrBlank()) {
                IconWithText(
                    iconResId = R.drawable.ic_calendar,
                    contentDescriptionResId = R.string.cd_deadline_date,
                    text = deadline,
                    color = MaterialTheme.colorScheme.surfaceVariant
                )
            }

            Button(
                modifier = Modifier.fillMaxWidth(),
                enabled = !isNoAttempts,
                colors = ButtonDefaults.buttonColors()
                    .copy(containerColor = MaterialTheme.colorScheme.primary),
                shape = RoundedCornerShape(32.dp),
                onClick = onStartTestingClick
            ) {
                Text(
                    text = if (isNoAttempts) {
                        stringResource(R.string.no_attempts)
                    } else if (isFirstAttempt == null || isFirstAttempt) {
                        stringResource(R.string.begin_testing)
                    } else {
                        stringResource(R.string.try_again)
                    },
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    }
}

@Composable
private fun IconWithText(
    modifier: Modifier = Modifier,
    iconResId: Int,
    contentDescriptionResId: Int,
    text: String,
    color: Color
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(iconResId),
            tint = color,
            contentDescription = stringResource(contentDescriptionResId),
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = text,
            color = color,
            fontSize = 16.sp
        )
    }
    Spacer(modifier = Modifier.height(8.dp))
}

@Composable
private fun QuizNotFound(
    modifier: Modifier = Modifier,
    iconCdResId: Int,
    titleResId: Int,
    descriptionResId: Int
) {
    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_not_found),
            contentDescription = stringResource(iconCdResId),
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.surfaceTint
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = stringResource(titleResId),
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = stringResource(descriptionResId),
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.surfaceTint
        )
    }
}

private data class StatusOption(val title: String, val value: QuizStatus?)

private fun statusOptions(): List<StatusOption> = listOf(
    StatusOption("Все статусы", null),
    StatusOption("Опубликованные", QuizStatus.PUBLISHED),
    StatusOption("Черновики", QuizStatus.DRAFT)
)