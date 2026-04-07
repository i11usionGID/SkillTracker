package com.i11usion.feature.main.presentation.quiz.testing.test

import AppError
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.i11usion.core.domain.model.network.AppResult
import com.i11usion.core.domain.model.quiz.QuestionType
import com.i11usion.core.domain.model.quiz.attempt.AttemptStart
import com.i11usion.core.domain.model.quiz.attempt.AttemptSubmitAnswer
import com.i11usion.core.domain.model.quiz.attempt.AttemptSubmitRequest
import com.i11usion.core.domain.usecase.quiz.attempt.StartAttemptUseCase
import com.i11usion.core.domain.usecase.quiz.attempt.SubmitAttemptUseCase
import com.i11usion.core.ui.model.ErrorScreenModel
import com.i11usion.core.ui.model.UiError
import com.i11usion.core.ui.model.UiText
import com.i11usion.feature.main.manager.SnackbarManager
import com.i11usion.feature.main.mapper.toTestUiError
import kotlinx.coroutines.Job
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.time.Instant
import javax.inject.Inject
import kotlin.math.max

class TestViewModel @Inject constructor(
    private val startAttemptUseCase: StartAttemptUseCase,
    private val submitAttemptUseCase: SubmitAttemptUseCase,
    private val snackbarManager: SnackbarManager
) : ViewModel() {

    private companion object {
        const val RETRY_DELAY_MS = 10_000L
    }

    private var currentQuizId: Int? = null
    private var timerJob: Job? = null
    private var submitJob: Job? = null
    private var loadJob: Job? = null

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

            TestCommand.FinishClicked -> finishAttempt(finishedByTimer = false)
        }
    }

    private fun init(quizId: Int) {
        currentQuizId = quizId
        loadJob?.cancel()
        timerJob?.cancel()

        loadJob = viewModelScope.launch {
            _state.value = TestUiState.Loading

            when (val result = retryRequest { startAttemptUseCase(quizId) }) {
                is AppResult.Success -> {
                    val initialAnswers = result.data.questions.associate { q ->
                        q.attemptQuestionId to UserAnswer.Empty(q.question.type)
                    }

                    val content = TestUiState.Content(
                        attempt = result.data,
                        answers = initialAnswers,
                        remainingSeconds = computeRemainingSeconds(
                            result.data.startedAt,
                            result.data.timeLimitSeconds
                        ),
                        isFinishing = false,
                        fullScreenError = null
                    )
                    _state.value = content

                    startTimerIfNeeded(result.data)
                }

                is AppResult.Failure -> {
                    showUiError(result.error)
                    if (result.error is AppError.Validation) {
                        _events.tryEmit(TestEvent.Back)
                    }
                }
            }
        }
    }

    private suspend fun <T> retryRequest(
        block: suspend () -> AppResult<T>
    ): AppResult<T> {
        while (true) {
            when (val result = block()) {
                is AppResult.Success -> return result
                is AppResult.Failure -> {
                    if (!shouldRetry(result.error)) return result
                    delay(RETRY_DELAY_MS)
                }
            }
        }
    }

    private fun shouldRetry(error: AppError): Boolean {
        return when (error) {
            is AppError.Network -> {
                snackbarManager.show(UiText.Dynamic("Отсутствует подключение к интернету"))
                true
            }

            else -> false
        }
    }

    private fun startTimerIfNeeded(attempt: AttemptStart) {
        val limit = (attempt.timeLimitSeconds?.minus(1)) ?: return
        timerJob?.cancel()

        timerJob = viewModelScope.launch {
            while (currentCoroutineContext().isActive) {
                val remaining = computeRemainingSeconds(attempt.startedAt, limit)

                val current = _state.value
                if (current is TestUiState.Content) {
                    _state.value = current.copy(remainingSeconds = remaining)
                }

                if ((remaining ?: 0) <= 0) {
                    finishAttempt(finishedByTimer = true)
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

    private fun finishAttempt(finishedByTimer: Boolean) {
        val s = _state.value as? TestUiState.Content ?: return
        if (s.isFinishing) return

        if (!finishedByTimer && (s.remainingSeconds ?: 1) <= 0) {
            finishAttempt(finishedByTimer = true)
            return
        }

        timerJob?.cancel()

        _state.value = s.copy(
            isFinishing = true,
            remainingSeconds = if (finishedByTimer) 0 else s.remainingSeconds
        )

        submitJob?.cancel()
        submitJob = viewModelScope.launch {
            val request = buildSubmitRequest(s.answers)

            when (val result = submitAttemptUseCase(
                attemptId = s.attempt.attemptId,
                request = request
            )) {
                is AppResult.Success -> {
                    _events.tryEmit(
                        TestEvent.NavigateToAnalytics(
                            attemptId = result.data.id,
                            finishedByTimer = finishedByTimer
                        )
                    )
                }

                is AppResult.Failure -> {
                    if (finishedByTimer) {
                        _state.value = TestUiState.Timeout
                    } else {
                        val current = _state.value as? TestUiState.Content
                        if (current != null) {
                            _state.value = current.copy(isFinishing = false)
                            startTimerIfNeeded(current.attempt)
                        }
                    }
                    showUiError(result.error)
                }
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

    private fun showUiError(error: AppError) {
        when (val uiError = error.toTestUiError()) {
            is UiError.SnackBar -> snackbarManager.show(uiError.message)

            is UiError.FullScreen -> {
                val current = _state.value
                if (current is TestUiState.Content) {
                    _state.value = current.copy(fullScreenError = uiError.model)
                } else {
                    _state.value = TestUiState.Error(uiError.model)
                }
            }
        }
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
    data object Back : TestEvent
}

sealed interface TestUiState {
    data object Initial : TestUiState
    data object Loading : TestUiState

    data class Content(
        val attempt: AttemptStart,
        val answers: Map<Int, UserAnswer>,
        val remainingSeconds: Int?,
        val isFinishing: Boolean,
        val fullScreenError: ErrorScreenModel? = null
    ) : TestUiState {
        val totalQuestions: Int get() = attempt.questions.size
        val answeredCount: Int get() = answers.values.count { it.isAnswered() }
    }

    data object Timeout : TestUiState

    data class Error(val model: ErrorScreenModel) : TestUiState
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