package com.i11usion.feature.main.presentation.quiz.testing.test

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.i11usion.core.domain.model.quiz.QuestionType
import com.i11usion.core.ui.di.LocalViewModelFactory
import com.i11usion.core.ui.helpers.asString
import com.i11usion.core.ui.template.ErrorScreen
import com.i11usion.feature.main.R
import kotlinx.coroutines.flow.collectLatest

@Composable
fun TestScreen(
    modifier: Modifier = Modifier,
    quizId: Int,
    onNavigateToAnalytics: (attemptId: Int) -> Unit,
    onBack: () -> Unit
) {
    val viewModel: TestViewModel = viewModel(factory = LocalViewModelFactory.current)
    val state by viewModel.state.collectAsState()

    val context = LocalContext.current

    LaunchedEffect(quizId) {
        viewModel.processCommand(TestCommand.Init(quizId))
    }

    LaunchedEffect(Unit) {
        viewModel.events.collectLatest { event ->
            when (event) {
                is TestEvent.NavigateToAnalytics -> onNavigateToAnalytics(event.attemptId)
                is TestEvent.Back -> onBack()
            }
        }
    }

    when (val s = state) {
        TestUiState.Initial -> Unit
        TestUiState.Loading -> LoadingContent(modifier)
        TestUiState.Timeout -> TimeoutContent(
            modifier = modifier,
            onBack = onBack
        )

        is TestUiState.Error -> {
            ErrorScreen(
                modifier = modifier.fillMaxSize(),
                iconResId = s.model.iconResId,
                cdResId = s.model.cdResId,
                errorText = s.model.text.asString(context),
                onBackClick = onBack
            )
        }

        is TestUiState.Content -> {

            val fsError = s.fullScreenError
            if (fsError != null) {
                ErrorScreen(
                    modifier = modifier.fillMaxSize(),
                    iconResId = fsError.iconResId,
                    cdResId = fsError.cdResId,
                    errorText = fsError.text.asString(LocalContext.current),
                    onBackClick = onBack
                )
                return
            }

            TestContent(
                modifier = modifier,
                state = s,
                onCommand = viewModel::processCommand
            )
        }
    }
}

@Composable
private fun LoadingContent(modifier: Modifier = Modifier) {
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

@Composable
private fun TimeoutContent(
    modifier: Modifier = Modifier,
    onBack: () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.Center
    ) {

        Card(
            modifier = Modifier,
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
                Text(
                    text = "Вы не успели завершить тест до окончания таймера",
                    fontSize = 18.sp,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(16.dp))
                Button(
                    onClick = onBack,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = stringResource(R.string.back_to_tests),
                        fontSize = 16.sp,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
private fun TestContent(
    modifier: Modifier = Modifier,
    state: TestUiState.Content,
    onCommand: (TestCommand) -> Unit
) {
    val answered = state.answeredCount
    val total = state.totalQuestions
    val progress = if (total == 0) 0f else answered.toFloat() / total.toFloat()


    Column(
        modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text(
                    "Прохождение теста",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    "Отвечено: $answered из $total",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            TimerBadge(remainingSeconds = state.remainingSeconds)
        }

        Spacer(Modifier.height(12.dp))
        LinearProgressIndicator(progress = { progress }, modifier = Modifier.fillMaxWidth())

        Spacer(Modifier.height(12.dp))
        Button(
            onClick = { onCommand(TestCommand.FinishClicked) },
            enabled = !state.isFinishing,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                if (state.isFinishing) "Отправляем..." else "Завершить тест",
                fontSize = 16.sp,
                color = Color.White
            )
        }

        Spacer(Modifier.height(8.dp))

        LazyColumn(
            modifier = Modifier
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Spacer(Modifier.height(8.dp))
            }

            items(
                items = state.attempt.questions.sortedBy { it.orderIndex },
                key = { it.attemptQuestionId }
            ) { item ->
                val typeText = when (item.question.type) {
                    QuestionType.SINGLE -> "Одиночный выбор"
                    QuestionType.MULTIPLE -> "Множественный выбор"
                    QuestionType.TEXT -> "Текстовый ответ"
                }

                QuestionCard(
                    index = item.orderIndex + 1,
                    attemptQuestionId = item.attemptQuestionId,
                    type = item.question.type,
                    typeText = typeText,
                    text = item.question.text,
                    options = item.question.options.map { it.id to it.text },
                    answer = state.answers[item.attemptQuestionId],
                    onCommand = onCommand,
                    enabled = !state.isFinishing
                )
            }

            item {
                Spacer(Modifier.height(8.dp))

                Card(
                    modifier = Modifier,
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
                        Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text(
                                "Готовы завершить тест?",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                "Вы ответили на $answered из $total вопросов",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                        Button(
                            onClick = { onCommand(TestCommand.FinishClicked) },
                            enabled = !state.isFinishing
                        ) {
                            Text(
                                "Завершить тест",
                                fontSize = 16.sp,
                                color = Color.White
                            )
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun TimerBadge(remainingSeconds: Int?) {
    val text = when (remainingSeconds) {
        null -> "∞"
        else -> formatAsMmSs(remainingSeconds)
    }

    val timerColor = if (remainingSeconds != null && remainingSeconds < 180) {
        MaterialTheme.colorScheme.error
    } else {
        MaterialTheme.colorScheme.onSurface
    }

    Text(
        text = text,
        fontSize = 24.sp,
        fontWeight = FontWeight.Bold,
        color = timerColor
    )
}

@Composable
fun QuestionCard(
    modifier: Modifier = Modifier,
    index: Int,
    attemptQuestionId: Int,
    type: QuestionType,
    typeText: String,
    text: String,
    options: List<Pair<Int, String>>,
    answer: UserAnswer?,
    onCommand: (TestCommand) -> Unit,
    enabled: Boolean
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
            Row(Modifier.fillMaxWidth()) {
                AssistChip(onClick = {}, enabled = false, label = { Text(index.toString()) })
                Spacer(Modifier.width(8.dp))
                AssistChip(onClick = {}, enabled = false, label = { Text(typeText) })
            }

            Spacer(Modifier.height(12.dp))
            Text(text, style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(12.dp))

            when (type) {
                QuestionType.TEXT -> {
                    val current = (answer as? UserAnswer.Text)?.text.orEmpty()
                    OutlinedTextField(
                        value = current,
                        onValueChange = {
                            onCommand(
                                TestCommand.Answer.TextChanged(
                                    attemptQuestionId,
                                    it
                                )
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = enabled,
                        placeholder = { Text("Введите ваш ответ…") },
                        minLines = 4
                    )
                }

                QuestionType.SINGLE -> {
                    val selected = (answer as? UserAnswer.Single)?.optionId

                    options.forEach { (id, optionText) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .selectable(
                                    selected = selected == id,
                                    enabled = enabled,
                                    role = Role.RadioButton,
                                    onClick = {
                                        onCommand(
                                            TestCommand.Answer.Single(
                                                attemptQuestionId,
                                                id
                                            )
                                        )
                                    }
                                )
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            RadioButton(
                                selected = selected == id,
                                onClick = null,
                                enabled = enabled
                            )
                            Text(optionText)
                        }
                    }
                }

                QuestionType.MULTIPLE -> {
                    val selected = (answer as? UserAnswer.Multiple)?.optionIds ?: emptySet()

                    options.forEach { (id, optionText) ->
                        val isChecked = selected.contains(id)

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .toggleable(
                                    value = isChecked,
                                    enabled = enabled,
                                    role = Role.Checkbox,
                                    onValueChange = { checked ->
                                        onCommand(
                                            TestCommand.Answer.MultipleToggle(
                                                attemptQuestionId,
                                                id,
                                                checked
                                            )
                                        )
                                    }
                                )
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Checkbox(
                                checked = isChecked,
                                onCheckedChange = null,
                                enabled = enabled
                            )
                            Text(optionText)
                        }
                    }
                }
            }
        }
    }
}

private fun formatAsMmSs(totalSeconds: Int): String {
    val clamped = if (totalSeconds < 0) 0 else totalSeconds
    val minutes = clamped / 60
    val seconds = clamped % 60
    return "%d:%02d".format(minutes, seconds)
}
