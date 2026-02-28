package com.i11usion.feature.main.presentation.quiz.generate

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.i11usion.core.domain.usecase.quiz.GenerateQuizUseCase
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

class GenerateQuizViewModel @Inject constructor(
    private val generateQuizUseCase: GenerateQuizUseCase
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

            try {
                generateQuizUseCase(content.topic)
                _state.value =
                    GenerateQuizUiState.Generating(topic = (_state.value as GenerateQuizUiState.Content).topic)
            } catch (t: Throwable) {
                _state.update { cur ->
                    val c = cur as? GenerateQuizUiState.Content ?: return@update cur
                    c.copy(isCreating = false, canCreate = computeCanCreate(c))
                }
                _events.emit(GenerateQuizEvent.ShowError(t.message ?: "Failed to create quiz"))
            }
        }
    }

    private fun updateContent(
        reducer: (GenerateQuizUiState.Content) -> GenerateQuizUiState.Content
    ) {
        _state.update { current ->
            val content = current as? GenerateQuizUiState.Content ?: GenerateQuizUiState.Content()
            val updated = reducer(content)
            updated.copy(canCreate = computeCanCreate(updated))
        }
    }

    private fun computeCanCreate(c: GenerateQuizUiState.Content): Boolean {
        val titleOk = c.topic.trim().isNotEmpty() && c.topic.length >= 2 && c.topic.length <= 255
        return titleOk && !c.isCreating
    }
}

sealed interface GenerateQuizEvent {
    data object Close : GenerateQuizEvent
    data class ShowError(val message: String) : GenerateQuizEvent
}

sealed interface GenerateQuizCommand {
    data class ChangeTopic(val query: String) : GenerateQuizCommand
    data object Generate : GenerateQuizCommand
    data object Cancel : GenerateQuizCommand
}

sealed interface GenerateQuizUiState {
    data object Initial : GenerateQuizUiState
    data class Content(
        val topic: String = "",
        val canCreate: Boolean = false,
        val isCreating: Boolean = false,
        val isSuccess: Boolean = false
    ) : GenerateQuizUiState

    data class Generating(
        val topic: String = ""
    ) : GenerateQuizUiState

    data class Error(
        val message: String
    ) : GenerateQuizUiState
}