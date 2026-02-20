package com.i11usion.feature.main.presentation.quiz.testing.test

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.i11usion.core.domain.model.quiz.QuestionType
import com.i11usion.core.domain.model.quiz.attempt.AttemptStart
import com.i11usion.core.domain.model.quiz.attempt.AttemptSubmitAnswer
import com.i11usion.core.domain.model.quiz.attempt.AttemptSubmitRequest
import com.i11usion.core.domain.usecase.quiz.attempt.StartAttemptUseCase
import com.i11usion.core.domain.usecase.quiz.attempt.SubmitAttemptUseCase
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.Instant
import javax.inject.Inject
import kotlin.math.max

class TestViewModel @Inject constructor(
    private val startAttemptUseCase: StartAttemptUseCase,
    private val submitAttemptUseCase: SubmitAttemptUseCase
) : ViewModel() {

    private var currentQuizId: Int? = null
    private var timerJob: Job? = null

    private val _state = MutableStateFlow<TestUiState>(TestUiState.Initial)
    val state = _state.asStateFlow()

    private val _events = MutableSharedFlow<TestEvent>(extraBufferCapacity = 1)
    val events = _events.asSharedFlow()

    fun processCommand(command: TestCommand) {
        when (command) {
            is TestCommand.Init -> init(command.quizId)

            is TestCommand.Answer.Single ->
                onSingleSelected(command.attemptQuestionId, command.optionId)

            is TestCommand.Answer.MultipleToggle ->
                onMultipleToggled(command.attemptQuestionId, command.optionId, command.isChecked)

            is TestCommand.Answer.TextChanged ->
                onTextChanged(command.attemptQuestionId, command.text)

            TestCommand.FinishClicked -> finishAttempt()
        }
    }

    private fun init(quizId: Int) {
        currentQuizId = quizId
        timerJob?.cancel()
        _state.value = TestUiState.Loading

        viewModelScope.launch {
            runCatching {
                startAttemptUseCase(quizId)
            }.onSuccess { attemptStart ->
                val initialAnswers = attemptStart.questions.associate { q ->
                    q.attemptQuestionId to UserAnswer.Empty(q.question.type)
                }

                val content = TestUiState.Content(
                    attempt = attemptStart,
                    answers = initialAnswers,
                    remainingSeconds = computeRemainingSeconds(
                        attemptStart.startedAt,
                        attemptStart.timeLimitSeconds
                    ),
                    isFinishing = false
                )
                _state.value = content

                startTimerIfNeeded(attemptStart)
            }.onFailure { t ->
                _state.value = TestUiState.Error(t.message ?: "Не удалось начать тест")
            }
        }
    }

    private fun startTimerIfNeeded(attempt: AttemptStart) {
        val limit = attempt.timeLimitSeconds ?: return
        timerJob?.cancel()

        timerJob = viewModelScope.launch {
            while (true) {
                val remaining = computeRemainingSeconds(attempt.startedAt, limit)

                val current = _state.value
                if (current is TestUiState.Content) {
                    _state.value = current.copy(remainingSeconds = remaining)
                }

                if ((remaining ?: 0) <= 0) {
                    timerJob?.cancel()
                    _state.value = TestUiState.Timeout
                    break
                }

                delay(1_000)
            }
        }
    }


    private fun computeRemainingSeconds(startedAt: Instant, limitSeconds: Int?): Int? {
        if (limitSeconds == null) return null
        val end = startedAt.epochSecond + limitSeconds.toLong()
        val now = Instant.now().epochSecond
        return max(0, (end - now).toInt())
    }

    private fun onSingleSelected(attemptQuestionId: Int, optionId: Int) {
        val s = _state.value
        if (s !is TestUiState.Content || s.isFinishing) return
        _state.value = s.copy(
            answers = s.answers + (attemptQuestionId to UserAnswer.Single(optionId))
        )
    }

    private fun onMultipleToggled(attemptQuestionId: Int, optionId: Int, isChecked: Boolean) {
        val s = _state.value
        if (s !is TestUiState.Content || s.isFinishing) return

        val current = s.answers[attemptQuestionId] as? UserAnswer.Multiple
            ?: UserAnswer.Multiple(emptySet())

        val nextSet = if (isChecked) current.optionIds + optionId else current.optionIds - optionId

        _state.value = s.copy(
            answers = s.answers + (attemptQuestionId to UserAnswer.Multiple(nextSet))
        )
    }

    private fun onTextChanged(attemptQuestionId: Int, text: String) {
        val s = _state.value
        if (s !is TestUiState.Content || s.isFinishing) return
        _state.value = s.copy(
            answers = s.answers + (attemptQuestionId to UserAnswer.Text(text))
        )
    }

    private fun finishAttempt() {
        val s = _state.value
        if (s !is TestUiState.Content || s.isFinishing) return

        if ((s.remainingSeconds ?: 1) <= 0) {
            _state.value = TestUiState.Timeout
            return
        }

        timerJob?.cancel()
        _state.value = s.copy(
            isFinishing = true,
            remainingSeconds = s.remainingSeconds
        )

        viewModelScope.launch {
            val request = buildSubmitRequest(s.answers)

            runCatching {
                submitAttemptUseCase(
                    attemptId = s.attempt.attemptId,
                    request = request
                )
            }.onSuccess { submitResult ->
                _events.tryEmit(
                    TestEvent.NavigateToAnalytics(
                        attemptId = submitResult.id,
                        finishedByTimer = false
                    )
                )
            }.onFailure { t ->
                _state.value = TestUiState.Error(t.message ?: "Не удалось отправить ответы")
            }
        }
    }

    private fun buildSubmitRequest(answers: Map<Int, UserAnswer>): AttemptSubmitRequest {
        val list = answers.mapNotNull { (attemptQuestionId, ans) ->
            when (ans) {
                is UserAnswer.Empty -> null

                is UserAnswer.Single -> AttemptSubmitAnswer(
                    attemptQuestionId = attemptQuestionId,
                    selectedOptionIds = listOf(ans.optionId),
                    textAnswer = null
                )

                is UserAnswer.Multiple -> {
                    if (ans.optionIds.isEmpty()) null
                    else AttemptSubmitAnswer(
                        attemptQuestionId = attemptQuestionId,
                        selectedOptionIds = ans.optionIds.toList(),
                        textAnswer = null
                    )
                }

                is UserAnswer.Text -> {
                    val t = ans.text.trim()
                    if (t.isEmpty()) null
                    else AttemptSubmitAnswer(
                        attemptQuestionId = attemptQuestionId,
                        selectedOptionIds = emptyList(),
                        textAnswer = t
                    )
                }
            }
        }
        return AttemptSubmitRequest(answers = list)
    }
}

sealed interface TestCommand {
    data class Init(val quizId: Int) : TestCommand

    sealed interface Answer : TestCommand {
        data class Single(val attemptQuestionId: Int, val optionId: Int) : Answer
        data class MultipleToggle(
            val attemptQuestionId: Int,
            val optionId: Int,
            val isChecked: Boolean
        ) : Answer

        data class TextChanged(val attemptQuestionId: Int, val text: String) : Answer
    }

    data object FinishClicked : TestCommand
}

sealed interface TestEvent {
    data class NavigateToAnalytics(val attemptId: Int, val finishedByTimer: Boolean) : TestEvent
}

sealed interface TestUiState {
    data object Initial : TestUiState
    data object Loading : TestUiState

    data class Content(
        val attempt: AttemptStart,
        val answers: Map<Int, UserAnswer>,
        val remainingSeconds: Int?,
        val isFinishing: Boolean
    ) : TestUiState {
        val totalQuestions: Int get() = attempt.questions.size
        val answeredCount: Int get() = answers.values.count { it.isAnswered() }
    }

    data object Timeout : TestUiState

    data class Error(val message: String) : TestUiState
}

sealed interface UserAnswer {
    fun isAnswered(): Boolean

    data class Empty(val type: QuestionType) : UserAnswer {
        override fun isAnswered() = false
    }

    data class Single(val optionId: Int) : UserAnswer {
        override fun isAnswered() = true
    }

    data class Multiple(val optionIds: Set<Int>) : UserAnswer {
        override fun isAnswered() = optionIds.isNotEmpty()
    }

    data class Text(val text: String) : UserAnswer {
        override fun isAnswered() = text.trim().isNotEmpty()
    }
}

