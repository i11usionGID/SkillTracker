package com.i11usion.feature.main.presentation.quiz.create

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.i11usion.core.domain.model.quiz.QuizCreate
import com.i11usion.core.domain.usecase.quiz.CreateQuizUseCase
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

class CreateQuizViewModel @Inject constructor(
    private val createQuizUseCase: CreateQuizUseCase
) : ViewModel() {

    private val _state = MutableStateFlow<CreateQuizUiState>(
        CreateQuizUiState.Initial)
        val state = _state . asStateFlow ()

    private val _events = MutableSharedFlow<CreateQuizEvent>()
    val events = _events.asSharedFlow()

    init {
        _state.value = CreateQuizUiState.Content()
    }

    fun processCommand(command: CreateQuizCommand) {
        when (command) {
            is CreateQuizCommand.ChangeTitle ->
                updateContent { it.copy(titleQuery = command.query) }

            is CreateQuizCommand.ChangeDescription ->
                updateContent { it.copy(descriptionQuery = command.query.trim().takeIf { s -> s.isNotEmpty() }) }

            is CreateQuizCommand.ChangePassPercent ->
                updateContent { it.copy(pasPercent = command.value.coerceIn(0, 100)) }

            is CreateQuizCommand.ChangeTimeLimitSeconds ->
                updateContent {
                    it.copy(timeLimitSeconds = command.value.takeIf { v -> v > 0 })
                }

            is CreateQuizCommand.ChangeMaxAttempts ->
                updateContent { it.copy(maxAttempts = command.value.coerceIn(1, 10)) }

            CreateQuizCommand.TogglePublish ->
                updateContent { it.copy(publish = !it.publish) }

            CreateQuizCommand.ToggleShuffleAnswers ->
                updateContent { it.copy(shuffleAnswers = !it.shuffleAnswers) }

            CreateQuizCommand.ToggleShuffleQuestions ->
                updateContent { it.copy(shuffleQuestions = !it.shuffleQuestions) }

            CreateQuizCommand.ToggleAllowReview ->
                updateContent { it.copy(allowReview = !it.allowReview) }

            CreateQuizCommand.Create -> create()

            CreateQuizCommand.Cancel -> {
                viewModelScope.launch { _events.emit(CreateQuizEvent.Close) }
            }
        }
    }

    private fun updateContent(
        reducer: (CreateQuizUiState.Content) -> CreateQuizUiState.Content
    ) {
        _state.update { current ->
            val content = current as? CreateQuizUiState.Content ?: CreateQuizUiState.Content()
            val updated = reducer(content)
            updated.copy(canCreate = computeCanCreate(updated))
        }
    }

    private fun create() {
        val content = _state.value as? CreateQuizUiState.Content ?: return
        if (!content.canCreate || content.isCreating) return

        viewModelScope.launch {
            _state.update { cur ->
                val c = cur as? CreateQuizUiState.Content ?: return@update cur
                c.copy(isCreating = true, canCreate = false)
            }

            try {
                createQuizUseCase(content.toDomainForCreate())
                _events.emit(CreateQuizEvent.Created)
            } catch (t: Throwable) {
                _state.update { cur ->
                    val c = cur as? CreateQuizUiState.Content ?: return@update cur
                    c.copy(isCreating = false, canCreate = computeCanCreate(c))
                }
                _events.emit(CreateQuizEvent.ShowError(t.message ?: "Failed to create quiz"))
            }
        }
    }

    private fun computeCanCreate(c: CreateQuizUiState.Content): Boolean {
        val titleOk = c.titleQuery.trim().isNotEmpty()
        return titleOk && !c.isCreating
    }

    private fun CreateQuizUiState.Content.toDomainForCreate(): QuizCreate {
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

sealed interface CreateQuizEvent {
    data object Created : CreateQuizEvent
    data object Close : CreateQuizEvent
    data class ShowError(val message: String) : CreateQuizEvent
}

sealed interface CreateQuizCommand {
    data class ChangePassPercent(val value: Int) : CreateQuizCommand
    data class ChangeTitle(val query: String) : CreateQuizCommand
    data class ChangeDescription(val query: String) : CreateQuizCommand
    data class ChangeTimeLimitSeconds(val value: Int) : CreateQuizCommand
    data class ChangeMaxAttempts(val value: Int) : CreateQuizCommand
    data object TogglePublish : CreateQuizCommand
    data object ToggleShuffleAnswers : CreateQuizCommand
    data object ToggleShuffleQuestions : CreateQuizCommand
    data object ToggleAllowReview : CreateQuizCommand
    data object Create : CreateQuizCommand
    data object Cancel : CreateQuizCommand
}

sealed interface CreateQuizUiState {
    data object Initial : CreateQuizUiState
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
        val canCreate: Boolean = false,
        val isCreating: Boolean = false
    ) : CreateQuizUiState

    data class Error(
        val message: String
    ) : CreateQuizUiState
}

