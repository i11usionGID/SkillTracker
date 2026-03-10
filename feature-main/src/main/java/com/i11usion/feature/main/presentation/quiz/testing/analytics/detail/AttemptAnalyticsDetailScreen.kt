package com.i11usion.feature.main.presentation.quiz.testing.analytics.detail

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.i11usion.core.domain.model.user.Role
import com.i11usion.core.ui.di.LocalViewModelFactory
import com.i11usion.core.ui.helpers.asString
import com.i11usion.core.ui.template.ErrorScreen
import com.i11usion.feature.main.R

@Composable
fun AttemptAnalyticsDetailScreen(
    modifier: Modifier = Modifier,
    attemptId: Int,
    onBack: () -> Unit
) {
    val viewModel: AttemptAnalyticsDetailViewModel =
        viewModel(factory = LocalViewModelFactory.current)
    val uiState by viewModel.state.collectAsState()
    val state = uiState

    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                AttemptAnalyticsDetailEvent.Finish -> onBack()
            }
        }
    }

    LaunchedEffect(attemptId) {
        viewModel.processCommand(AttemptAnalyticsDetailCommand.Init(attemptId))
    }

    when (state) {
        AttemptAnalyticsDetailUiState.Initial -> {}
        AttemptAnalyticsDetailUiState.Loading -> {
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

        is AttemptAnalyticsDetailUiState.Content -> {

            val fsError = state.fullScreenError
            if (fsError != null) {
                ErrorScreen(
                    modifier = modifier.fillMaxSize(),
                    iconResId = fsError.iconResId,
                    cdResId = fsError.cdResId,
                    errorText = fsError.text.asString(LocalContext.current),
                    onRefreshClick = { viewModel.processCommand(AttemptAnalyticsDetailCommand.Refresh) },
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

                    ResultCard(
                        userRole = state.user,
                        scorePercent = state.attempt.scorePercent,
                        passed = state.attempt.passed,
                        startedAt = state.attempt.startedAt,
                        finishedAt = state.attempt.finishedAt,
                        onBackClick = { onBack() }
                    )

                    Spacer(modifier = Modifier.height(32.dp))
                }

                if (state.attempt.answers.isEmpty()) {
                    item {
                        UnavailableAnswers()

                        Spacer(modifier = Modifier.height(16.dp))
                    }
                } else {
                    itemsIndexed(
                        items = state.attempt.answers,
                        key = { _, answer -> answer.attemptQuestionId }
                    ) { index, answer ->
                        AnswerCard(
                            questionId = index + 1,
                            userRole = state.user,
                            isCorrect = answer.isCorrect,
                            selectedOptionIds = answer.selectedOptionIds,
                            textAnswer = answer.textAnswer
                        )

                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
        }

        is AttemptAnalyticsDetailUiState.Error -> {
            ErrorScreen(
                modifier = modifier.fillMaxSize(),
                iconResId = state.model.iconResId,
                cdResId = state.model.cdResId,
                errorText = state.model.text.asString(context),
                onRefreshClick = { viewModel.processCommand(AttemptAnalyticsDetailCommand.Refresh) },
                onBackClick = onBack
            )
        }
    }
}

@Composable
private fun ResultCard(
    modifier: Modifier = Modifier,
    userRole: Role,
    scorePercent: Double?,
    passed: Boolean?,
    startedAt: String,
    finishedAt: String?,
    onBackClick: () -> Unit
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
        Column(
            modifier = Modifier.padding(vertical = 24.dp, horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val passedColor: Color
            val passedContainerColor: Color
            val passedTitle: String

            if (passed!!) {
                passedColor = MaterialTheme.colorScheme.tertiary
                passedContainerColor = MaterialTheme.colorScheme.tertiaryContainer
                passedTitle = stringResource(R.string.quiz_passed)
            } else {
                passedColor = MaterialTheme.colorScheme.error
                passedContainerColor = MaterialTheme.colorScheme.errorContainer
                passedTitle = stringResource(R.string.test_not_passed)
            }

            Icon(
                painter = if (passed) {
                    painterResource(com.i11usion.core.ui.R.drawable.ic_logo)
                } else {
                    painterResource(R.drawable.ic_quiz_fail)
                },
                contentDescription = stringResource(R.string.cd_result_quiz_attempt),
                tint = passedColor,
                modifier = Modifier.size(64.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = passedTitle,
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Box(
                modifier = Modifier.background(
                    color = passedContainerColor,
                    shape = RoundedCornerShape(50.dp)
                ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                    text = stringResource(R.string.complete),
                    color = passedColor,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            val scorePercentText = String.format("%.1f%%", scorePercent)
            Text(
                text = scorePercentText,
                color = passedColor,
                fontSize = 48.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = if (userRole == Role.EMPLOYEE) {
                    stringResource(R.string.your_result)
                } else {
                    stringResource(R.string.result)
                },
                color = MaterialTheme.colorScheme.surfaceVariant,
                fontSize = 16.sp,
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth()
            ) {
                Spacer(modifier.weight(1f))

                DateBlock(
                    title = stringResource(R.string.begin),
                    date = startedAt
                )

                Spacer(modifier.width(8.dp))

                DateBlock(
                    title = stringResource(R.string.end),
                    date = finishedAt!!
                )

                Spacer(modifier.weight(1f))
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onBackClick,
                colors = ButtonDefaults.buttonColors()
                    .copy(containerColor = MaterialTheme.colorScheme.surfaceBright),
                shape = RoundedCornerShape(32.dp),
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Default.ArrowBack,
                    contentDescription = stringResource(R.string.cd_back),
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.width(4.dp))

                Text(
                    text = if (userRole == Role.EMPLOYEE) {
                        stringResource(R.string.to_my_quizzes)
                    } else {
                        stringResource(R.string.go_back)
                    },
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
private fun DateBlock(
    modifier: Modifier = Modifier,
    title: String,
    date: String,
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceBright,
            disabledContainerColor = MaterialTheme.colorScheme.surfaceBright
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
        Column(
            modifier = Modifier.padding(vertical = 8.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                color = MaterialTheme.colorScheme.surfaceVariant,
                fontSize = 14.sp,
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = date,
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 16.sp,
            )
        }
    }
}

@Composable
private fun UnavailableAnswers(
    modifier: Modifier = Modifier
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
        Column(
            modifier = Modifier.padding(vertical = 24.dp, horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_info),
                contentDescription = stringResource(R.string.cd_answers_details_not_available),
                modifier = Modifier.size(58.dp),
                tint = MaterialTheme.colorScheme.surfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = stringResource(R.string.answers_details_not_available),
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = stringResource(R.string.quiz_creator_limited_view_answers),
                color = MaterialTheme.colorScheme.surfaceVariant,
                fontSize = 16.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun AnswerCard(
    modifier: Modifier = Modifier,
    userRole: Role,
    questionId: Int,
    isCorrect: Boolean?,
    selectedOptionIds: List<Int>,
    textAnswer: String?
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
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 24.dp, horizontal = 16.dp)
        ) {
            val correctColor: Color
            val correctContainerColor: Color
            val correctStatus: String

            if (isCorrect!!) {
                correctColor = MaterialTheme.colorScheme.tertiary
                correctContainerColor = MaterialTheme.colorScheme.tertiaryContainer
                correctStatus = stringResource(R.string.correct)
            } else {
                correctColor = MaterialTheme.colorScheme.error
                correctContainerColor = MaterialTheme.colorScheme.errorContainer
                correctStatus = stringResource(R.string.incorrect)
            }

            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(
                        color = correctContainerColor,
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = if (isCorrect) {
                        painterResource(R.drawable.ic_check_mark)
                    } else {
                        painterResource(R.drawable.ic_x)
                    },
                    contentDescription = stringResource(R.string.cd_correct_answer),
                    modifier = Modifier.size(32.dp),
                    tint = correctColor
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.fillMaxWidth()) {

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.question) + " $questionId",
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 16.sp,
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Box(
                        modifier = Modifier.background(
                            color = correctContainerColor,
                            shape = RoundedCornerShape(50.dp)
                        ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                            text = correctStatus,
                            color = correctColor,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                if (selectedOptionIds.isEmpty() && !textAnswer.isNullOrBlank()) {

                    Text(
                        text = if (userRole == Role.EMPLOYEE) {
                            stringResource(R.string.your_answer)
                        } else {
                            stringResource(R.string.employee_answer)
                        },
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        fontSize = 16.sp,
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                color = correctContainerColor,
                                shape = RoundedCornerShape(12.dp)
                            )
                            .border(
                                width = 1.dp,
                                color = correctColor.copy(alpha = 0.5f),
                                shape = RoundedCornerShape(12.dp)
                            )
                    ) {
                        Text(
                            modifier = Modifier.padding(16.dp),
                            text = textAnswer,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 16.sp,
                        )
                    }
                } else if (textAnswer.isNullOrBlank() && selectedOptionIds.isNotEmpty()) {
                    Text(
                        text = stringResource(R.string.selected_options) + " ${selectedOptionIds.size}",
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        fontSize = 16.sp,
                    )
                }
            }
        }
    }
}