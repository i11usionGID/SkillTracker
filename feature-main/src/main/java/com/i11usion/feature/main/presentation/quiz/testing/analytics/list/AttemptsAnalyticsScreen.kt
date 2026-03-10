package com.i11usion.feature.main.presentation.quiz.testing.analytics.list

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.filled.Check
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.i11usion.core.domain.model.quiz.attempt.AttemptStatus
import com.i11usion.core.domain.model.user.User
import com.i11usion.core.ui.di.LocalViewModelFactory
import com.i11usion.core.ui.helpers.asString
import com.i11usion.core.ui.template.ErrorScreen
import com.i11usion.feature.main.R

@Composable
fun AttemptsAnalyticsScreen(
    modifier: Modifier = Modifier,
    quizId: Int,
    onBack: () -> Unit,
    onAttemptDetailScreen: (Int) -> Unit
) {
    val viewModel: AttemptsAnalyticsViewModel = viewModel(factory = LocalViewModelFactory.current)
    val uiState by viewModel.state.collectAsState()
    val state = uiState

    val context = LocalContext.current

    LaunchedEffect(quizId) {
        viewModel.processCommand(AttemptsAnalyticsCommand.Init(quizId))
    }


    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                AttemptsAnalyticsEvent.Finish -> onBack()
            }
        }
    }

    when (state) {
        AttemptsAnalyticsUiState.Initial -> {}

        is AttemptsAnalyticsUiState.Content -> {

            val fsError = state.fullScreenError
            if (fsError != null) {
                ErrorScreen(
                    modifier = modifier.fillMaxSize(),
                    iconResId = fsError.iconResId,
                    cdResId = fsError.cdResId,
                    errorText = fsError.text.asString(context),
                    onRefreshClick = {
                        viewModel.processCommand(AttemptsAnalyticsCommand.Refresh)
                    },
                    onBackClick = onBack
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
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = stringResource(R.string.quiz_solved_attempts),
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = { viewModel.processCommand(AttemptsAnalyticsCommand.FinishClicked) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors()
                            .copy(containerColor = MaterialTheme.colorScheme.surfaceBright),
                        shape = RoundedCornerShape(32.dp),
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                                contentDescription = stringResource(R.string.cd_back),
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = stringResource(R.string.go_back),
                                fontSize = 16.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }

                item {
                    FilterCard(
                        employees = state.users,
                        selectedEmployeeId = state.selectedEmployeeId,
                        onEmployeeChanged = { newId ->
                            viewModel.processCommand(
                                AttemptsAnalyticsCommand.EmployeeSelected(newId)
                            )
                        }
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                }

                if (state.isAttemptsListLoading) {
                    item {
                        Spacer(modifier = Modifier.height(16.dp))

                        Column(
                            modifier = modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                        }
                    }
                } else if (state.attempts.isEmpty()) {
                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                        EmptyAttemptList(
                            isUserSelected = state.selectedEmployeeId != null
                        )
                    }
                } else {
                    items(
                        items = state.attempts,
                        key = { attempt -> attempt.id }
                    ) { attempt ->
                        AttemptCard(
                            attemptStatus = attempt.status,
                            passed = attempt.passed,
                            scorePercent = attempt.scorePercent,
                            userName = attempt.userName,
                            startedAt = attempt.startedAt,
                            finishedAt = attempt.finishedAt,
                            onCheckDetailAttemptAnalytic = {
                                onAttemptDetailScreen(attempt.id)
                            }
                        )

                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
        }

        is AttemptsAnalyticsUiState.Error -> {
            ErrorScreen(
                modifier = modifier.fillMaxSize(),
                iconResId = state.model.iconResId,
                cdResId = state.model.cdResId,
                errorText = state.model.text.asString(context),
                onRefreshClick = {
                    viewModel.processCommand(AttemptsAnalyticsCommand.Refresh)
                },
                onBackClick = onBack
            )
        }
    }
}

@Composable
private fun FilterCard(
    modifier: Modifier = Modifier,
    employees: List<User>,
    selectedEmployeeId: Int?,
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
                text = stringResource(R.string.employee_filter),
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
private fun EmptyAttemptList(
    modifier: Modifier = Modifier,
    isUserSelected: Boolean
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_folder),
            contentDescription = stringResource(R.string.cd_no_attempts_from_user),
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.surfaceVariant
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = stringResource(R.string.no_attempts),
            fontSize = 24.sp,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = if (isUserSelected) {
                stringResource(R.string.this_employee_not_start_quiz_yet)
            } else {
                stringResource(R.string.employees_no_attempt_yet)
            },
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.surfaceVariant
        )
    }
}

@Composable
private fun AttemptCard(
    modifier: Modifier = Modifier,
    attemptStatus: AttemptStatus,
    passed: Boolean?,
    scorePercent: Double?,
    userName: String?,
    startedAt: String,
    finishedAt: String?,
    onCheckDetailAttemptAnalytic: () -> Unit
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {

                val statusText: String
                val statusColor: Color
                val statusContainerColor: Color

                when (attemptStatus) {
                    AttemptStatus.IN_PROGRESS -> {
                        statusText = "В процессе"
                        statusColor = MaterialTheme.colorScheme.primary
                        statusContainerColor = MaterialTheme.colorScheme.primaryContainer
                    }

                    AttemptStatus.COMPLETED -> {
                        if (passed!!) {
                            statusText = "Пройдено"
                            statusColor = MaterialTheme.colorScheme.tertiary
                            statusContainerColor = MaterialTheme.colorScheme.tertiaryContainer
                        } else {
                            statusText = "Не пройдено"
                            statusColor = MaterialTheme.colorScheme.error
                            statusContainerColor = MaterialTheme.colorScheme.errorContainer
                        }
                    }

                    AttemptStatus.EXPIRED -> {
                        statusText = "Истек срок"
                        statusColor = MaterialTheme.colorScheme.primary
                        statusContainerColor = MaterialTheme.colorScheme.primaryContainer
                    }
                }

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

                Spacer(modifier = Modifier.width(16.dp))

                if (scorePercent != null) {
                    val scorePercentText = String.format("%.1f%%", scorePercent)

                    Text(
                        text = scorePercentText,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = statusColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (!userName.isNullOrBlank()) {
                Row(modifier = Modifier.fillMaxWidth()) {

                    Text(
                        text = stringResource(R.string.user),
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.surfaceVariant
                    )

                    Spacer(modifier = Modifier.width(4.dp))

                    Text(
                        text = userName,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
            }

            Row(
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = stringResource(R.string.begin_at),
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.surfaceVariant
                )

                Spacer(modifier = Modifier.width(4.dp))

                Text(
                    text = startedAt,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            if (!finishedAt.isNullOrBlank()) {

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = stringResource(R.string.completed_at),
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.surfaceVariant
                    )

                    Spacer(modifier = Modifier.width(4.dp))

                    Text(
                        text = finishedAt,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            if (attemptStatus == AttemptStatus.COMPLETED) {
                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Button(
                        onClick = onCheckDetailAttemptAnalytic,
                        modifier = Modifier.height(32.dp),
                        colors = ButtonDefaults.buttonColors()
                            .copy(containerColor = MaterialTheme.colorScheme.surfaceBright),
                        shape = RoundedCornerShape(32.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = stringResource(R.string.check_detail),
                                fontSize = 16.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            Spacer(modifier = Modifier.width(4.dp))

                            Icon(
                                imageVector = Icons.AutoMirrored.Outlined.ArrowForward,
                                contentDescription = stringResource(R.string.check_attempt_detail),
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }
    }
}
