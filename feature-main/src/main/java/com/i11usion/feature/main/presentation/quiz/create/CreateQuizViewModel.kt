package com.i11usion.feature.main.presentation.quiz.create

import AppError
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.i11usion.core.domain.model.network.AppResult
import com.i11usion.core.domain.model.quiz.QuizCreate
import com.i11usion.core.domain.usecase.quiz.CreateQuizUseCase
import com.i11usion.core.ui.model.ErrorScreenModel
import com.i11usion.core.ui.model.UiError
import com.i11usion.feature.main.manager.SnackbarManager
import com.i11usion.feature.main.mapper.toScreenUiError
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

class CreateQuizViewModel @Inject constructor(
    private val createQuizUseCase: CreateQuizUseCase,
    private val snackbarManager: SnackbarManager
) : ViewModel() {

    private val _state = MutableStateFlow<CreateQuizUiState>(
        CreateQuizUiState.Initial
    )
    val state = _state.asStateFlow()

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
                updateContent {
                    it.copy(
                        descriptionQuery = command.query.trim().takeIf { s -> s.isNotEmpty() })
                }

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

            CreateQuizCommand.Refresh -> onErrorRefreshClick()
        }
    }

    private fun onErrorRefreshClick() {
        _state.update { state ->
            when (state) {
                is CreateQuizUiState.Content -> state.copy(fullScreenError = null)
                else -> CreateQuizUiState.Content()
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
            when (val result = createQuizUseCase(content.toDomainForCreate())) {
                is AppResult.Success -> {
                    _events.emit(CreateQuizEvent.Created)
                }

                is AppResult.Failure -> {
                    _state.update { state ->
                        val content = (state as? CreateQuizUiState.Content) ?: content
                        val base = content.copy(isCreating = false)
                        base.copy(canCreate = computeCanCreate(base))
                    }

                    showUiError(result.error)
                }
            }
        }
    }

    private fun computeCanCreate(c: CreateQuizUiState.Content): Boolean {
        val titleOk = c.titleQuery.trim().length in 1..255
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

    private fun showUiError(error: AppError) {
        when (val uiError = error.toScreenUiError()) {
            is UiError.SnackBar -> snackbarManager.show(uiError.message)

            is UiError.FullScreen -> {
                val current = _state.value
                if (current is CreateQuizUiState.Content) {
                    _state.value = current.copy(fullScreenError = uiError.model, isCreating = false)
                } else {
                    _state.value = CreateQuizUiState.Error(uiError.model)
                }
            }
        }
    }
}

sealed interface CreateQuizEvent {
    data object Created : CreateQuizEvent
    data object Close : CreateQuizEvent
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
    data object Refresh : CreateQuizCommand
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
        val isCreating: Boolean = false,
        val fullScreenError: ErrorScreenModel? = null
    ) : CreateQuizUiState

    data class Error(
        val model: ErrorScreenModel
    ) : CreateQuizUiState
}

