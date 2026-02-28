package com.i11usion.feature.main.presentation.quiz.update

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.i11usion.core.domain.model.quiz.QuizCreate
import com.i11usion.core.domain.model.quiz.QuizStatus
import com.i11usion.core.domain.usecase.quiz.GetQuizByIdUseCase
import com.i11usion.core.domain.usecase.quiz.UpdateQuizUseCase
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

class UpdateQuizViewModel @Inject constructor(
    private val getQuizByIdUseCase: GetQuizByIdUseCase,
    private val updateQuizUseCase: UpdateQuizUseCase
) : ViewModel() {

    private var currentQuizId: Int? = null

    private val _state = MutableStateFlow<UpdateQuizUiState>(UpdateQuizUiState.Initial)
    val state = _state.asStateFlow()

    private val _events = MutableSharedFlow<UpdateQuizEvent>()
    val events = _events.asSharedFlow()

    fun processCommand(command: UpdateQuizCommand) {
        when (command) {
            is UpdateQuizCommand.Init -> init(command.quizId)

            is UpdateQuizCommand.ChangeTitle ->
                updateContent { it.copy(titleQuery = command.query) }

            is UpdateQuizCommand.ChangeDescription ->
                updateContent { it.copy(descriptionQuery = command.query.trim().takeIf { s -> s.isNotEmpty() }) }

            is UpdateQuizCommand.ChangePassPercent ->
                updateContent { it.copy(pasPercent = command.value.coerceIn(0, 100)) }

            is UpdateQuizCommand.ChangeTimeLimitSeconds ->
                updateContent { it.copy(timeLimitSeconds = command.value.takeIf { v -> v > 0 }) }

            is UpdateQuizCommand.ChangeMaxAttempts ->
                updateContent { it.copy(maxAttempts = command.value.coerceIn(1, 10)) }

            UpdateQuizCommand.TogglePublish ->
                updateContent { it.copy(publish = !it.publish) }

            UpdateQuizCommand.ToggleShuffleAnswers ->
                updateContent { it.copy(shuffleAnswers = !it.shuffleAnswers) }

            UpdateQuizCommand.ToggleShuffleQuestions ->
                updateContent { it.copy(shuffleQuestions = !it.shuffleQuestions) }

            UpdateQuizCommand.ToggleAllowReview ->
                updateContent { it.copy(allowReview = !it.allowReview) }

            UpdateQuizCommand.Update -> update()

            UpdateQuizCommand.Cancel -> {
                viewModelScope.launch { _events.emit(UpdateQuizEvent.Close) }
            }
        }
    }

    private fun init(quizId: Int) {
        currentQuizId = quizId
        _state.value = UpdateQuizUiState.Loading

        viewModelScope.launch {
            try {
                val quiz = getQuizByIdUseCase(quizId)

                _state.value = UpdateQuizUiState.Content(
                    titleQuery = quiz.title,
                    descriptionQuery = quiz.description,
                    pasPercent = quiz.passPercent,
                    timeLimitSeconds = quiz.timeLimitSeconds,
                    maxAttempts = quiz.maxAttempts,
                    publish = when (quiz.status) {
                        QuizStatus.DRAFT -> false
                        QuizStatus.PUBLISHED -> true
                    },
                    shuffleAnswers = quiz.shuffleAnswers,
                    shuffleQuestions = quiz.shuffleQuestions,
                    allowReview = quiz.allowReview,
                    canUpdate = true,
                    isUpdating = false
                ).recompute()
            } catch (t: Throwable) {
                _state.value = UpdateQuizUiState.Error(t.message ?: "Failed to load quiz")
            }
        }
    }

    private fun updateContent(
        reducer: (UpdateQuizUiState.Content) -> UpdateQuizUiState.Content
    ) {
        _state.update { current ->
            val content = current as? UpdateQuizUiState.Content ?: return@update current
            reducer(content).recompute()
        }
    }

    private fun update() {
        val quizId = currentQuizId ?: return
        val content = _state.value as? UpdateQuizUiState.Content ?: return
        if (!content.canUpdate || content.isUpdating) return

        viewModelScope.launch {
            _state.update { cur ->
                val c = cur as? UpdateQuizUiState.Content ?: return@update cur
                c.copy(isUpdating = true, canUpdate = false)
            }

            try {
                updateQuizUseCase(
                    quizId = quizId,
                    quiz = content.toDomainForUpdate()
                )
                _events.emit(UpdateQuizEvent.Updated)
            } catch (t: Throwable) {
                _state.update { cur ->
                    val c = cur as? UpdateQuizUiState.Content ?: return@update cur
                    c.copy(isUpdating = false).recompute()
                }
                _events.emit(UpdateQuizEvent.ShowError(t.message ?: "Failed to update quiz"))
            }
        }
    }

    private fun UpdateQuizUiState.Content.recompute(): UpdateQuizUiState.Content {
        val titleOk = titleQuery.trim().isNotEmpty()
        return copy(canUpdate = titleOk && !isUpdating)
    }

    // Я использую QuizCreate, потому что у тебя уже есть такая модель.
    // Если UpdateQuizUseCase принимает другую модель — замени тут.
    private fun UpdateQuizUiState.Content.toDomainForUpdate(): QuizCreate {
        return QuizCreate(
            title = titleQuery.trim(),
            description = descriptionQuery?.trim()?.takeIf { it.isNotEmpty() },
            passPercent = pasPercent.coerceIn(0, 100),
            timeLimitSeconds = timeLimitSeconds?.takeIf { it > 0 },
            shuffleQuestions = shuffleQuestions,
            shuffleAnswers = shuffleAnswers,
            allowReview = allowReview,
            maxAttempts = maxAttempts?.takeIf { it > 0 },
            publish = publish
        )
    }
}

sealed interface UpdateQuizEvent {
    data object Updated : UpdateQuizEvent
    data object Close : UpdateQuizEvent
    data class ShowError(val message: String) : UpdateQuizEvent
}

sealed interface UpdateQuizCommand {
    data class Init(val quizId: Int) : UpdateQuizCommand
    data class ChangePassPercent(val value: Int) : UpdateQuizCommand
    data class ChangeTitle(val query: String) : UpdateQuizCommand
    data class ChangeDescription(val query: String) : UpdateQuizCommand
    data class ChangeTimeLimitSeconds(val value: Int) : UpdateQuizCommand
    data class ChangeMaxAttempts(val value: Int) : UpdateQuizCommand
    data object TogglePublish : UpdateQuizCommand
    data object ToggleShuffleAnswers : UpdateQuizCommand
    data object ToggleShuffleQuestions : UpdateQuizCommand
    data object ToggleAllowReview : UpdateQuizCommand
    data object Update : UpdateQuizCommand
    data object Cancel : UpdateQuizCommand
}

sealed interface UpdateQuizUiState {
    data object Initial : UpdateQuizUiState
    data object Loading : UpdateQuizUiState

    data class Content(
        val titleQuery: String = "",
        val descriptionQuery: String? = null,
        val pasPercent: Int = 70,
        val timeLimitSeconds: Int? = null,
        val maxAttempts: Int? = null,
        val publish: Boolean = false,
        val shuffleAnswers: Boolean = false,
        val shuffleQuestions: Boolean = false,
        val allowReview: Boolean = false,
        val canUpdate: Boolean = false,
        val isUpdating: Boolean = false
    ) : UpdateQuizUiState

    data class Error(val message: String) : UpdateQuizUiState
}
