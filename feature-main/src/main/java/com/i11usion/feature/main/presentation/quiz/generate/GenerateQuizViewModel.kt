package com.i11usion.feature.main.presentation.quiz.generate

import AppError
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.i11usion.core.domain.model.network.AppResult
import com.i11usion.core.domain.usecase.quiz.GenerateQuizUseCase
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

class GenerateQuizViewModel @Inject constructor(
    private val generateQuizUseCase: GenerateQuizUseCase,
    private val snackbarManager: SnackbarManager
) : ViewModel() {
    private val _state = MutableStateFlow<GenerateQuizUiState>(GenerateQuizUiState.Initial)
    val state = _state.asStateFlow()

    private val _events = MutableSharedFlow<GenerateQuizEvent>()
    val events = _events.asSharedFlow()

    init {
        _state.value = GenerateQuizUiState.Content()
    }

    fun processCommand(command: GenerateQuizCommand) {
        when (command) {
            is GenerateQuizCommand.ChangeTopic -> {
                updateContent { it.copy(topic = command.query) }
            }

            GenerateQuizCommand.Cancel -> {
                viewModelScope.launch { _events.emit(GenerateQuizEvent.Close) }
            }

            GenerateQuizCommand.Generate -> create()

            GenerateQuizCommand.Refresh -> onErrorRefreshClick()
        }
    }

    private fun onErrorRefreshClick() {
        _state.update { state ->
            when (state) {
                is GenerateQuizUiState.Content -> state.copy(fullScreenError = null)
                else -> GenerateQuizUiState.Content()
            }
        }
    }

    private fun create() {
        val content = _state.value as? GenerateQuizUiState.Content ?: return
        if (!content.canCreate || content.isCreating) return

        viewModelScope.launch {
            _state.update { cur ->
                val c = cur as? GenerateQuizUiState.Content ?: return@update cur
                c.copy(isCreating = true, canCreate = false)
            }

            when (val result = generateQuizUseCase(content.topic)) {

                is AppResult.Failure -> {
                    _state.update { state ->
                        val content = (state as? GenerateQuizUiState.Content) ?: content
                        val base = content.copy(isCreating = false)
                        base.copy(canCreate = computeCanCreate(base))
                    }

                    showUiError(result.error)
                }

                is AppResult.Success -> {
                    _state.value =
                        GenerateQuizUiState.Generating(
                            topic = (_state.value as GenerateQuizUiState.Content).topic
                        )
                }
            }
        }
    }

    private fun updateContent(
        reducer: (GenerateQuizUiState.Content) -> GenerateQuizUiState.Content
    ) {
        _state.update { current ->
            val content =
                current as? GenerateQuizUiState.Content ?: GenerateQuizUiState.Content()
            val updated = reducer(content)
            updated.copy(canCreate = computeCanCreate(updated))
        }
    }

    private fun computeCanCreate(c: GenerateQuizUiState.Content): Boolean {
        val titleOk =
            c.topic.trim().isNotEmpty() && c.topic.length >= 2 && c.topic.length <= 255
        return titleOk && !c.isCreating
    }

    private fun showUiError(error: AppError) {
        when (val uiError = error.toScreenUiError()) {
            is UiError.SnackBar -> snackbarManager.show(uiError.message)

            is UiError.FullScreen -> {
                val current = _state.value
                if (current is GenerateQuizUiState.Content) {
                    _state.value = current.copy(fullScreenError = uiError.model, isCreating = false)
                } else {
                    _state.value = GenerateQuizUiState.Error(uiError.model)
                }
            }
        }
    }
}

sealed interface GenerateQuizEvent {
    data object Close : GenerateQuizEvent
}

sealed interface GenerateQuizCommand {
    data class ChangeTopic(val query: String) : GenerateQuizCommand
    data object Generate : GenerateQuizCommand
    data object Cancel : GenerateQuizCommand
    data object Refresh : GenerateQuizCommand
}

sealed interface GenerateQuizUiState {
    data object Initial : GenerateQuizUiState
    data class Content(
        val topic: String = "",
        val canCreate: Boolean = false,
        val isCreating: Boolean = false,
        val isSuccess: Boolean = false,
        val fullScreenError: ErrorScreenModel? = null
    ) : GenerateQuizUiState

    data class Generating(
        val topic: String = ""
    ) : GenerateQuizUiState

    data class Error(
        val model: ErrorScreenModel
    ) : GenerateQuizUiState
}