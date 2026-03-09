package com.i11usion.feature.main.presentation.quiz.detail.question_section.create

import AppError
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.i11usion.core.domain.model.network.AppResult
import com.i11usion.core.domain.usecase.quiz.CreateQuestionSectionUseCase
import com.i11usion.core.domain.usecase.quiz.GetQuestionsUseCase
import com.i11usion.core.ui.model.ErrorScreenModel
import com.i11usion.core.ui.model.UiError
import com.i11usion.feature.main.manager.SnackbarManager
import com.i11usion.feature.main.mapper.toScreenUiError
import com.i11usion.feature.main.presentation.quiz.detail.question_section.create.mapper.domainToQuestionInQuestionSectionUiModel
import com.i11usion.feature.main.presentation.quiz.detail.question_section.create.model.QuestionInQuestionSectionUiModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

class CreateQuestionSectionViewModel @Inject constructor(
    private val getQuestionsUseCase: GetQuestionsUseCase,
    private val createSectionUseCase: CreateQuestionSectionUseCase,
    private val snackbarManager: SnackbarManager
) : ViewModel() {

    private var currentQuizId: Int? = null

    private val _state =
        MutableStateFlow<CreateQuestionSectionUiState>(CreateQuestionSectionUiState.Initial)
    val state = _state.asStateFlow()

    private val _events = MutableSharedFlow<CreateQuestionSectionEvent>(extraBufferCapacity = 1)
    val events = _events.asSharedFlow()

    fun processCommand(command: CreateQuestionSectionCommand) {
        when (command) {
            is CreateQuestionSectionCommand.Init -> init(command.quizId)
            CreateQuestionSectionCommand.CloseClicked -> _events.tryEmit(CreateQuestionSectionEvent.Close)

            is CreateQuestionSectionCommand.ChangeTitle ->
                updateContent { it.copy(titleQuery = command.value) }

            is CreateQuestionSectionCommand.ChangeDescription ->
                updateContent { it.copy(descriptionQuery = command.value) }

            is CreateQuestionSectionCommand.ChangeOrderIndex ->
                updateContent { it.copy(orderIndex = command.value.coerceIn(1, 100)) }

            is CreateQuestionSectionCommand.ChangePickCount ->
                updateContent { it.copy(pickCount = command.value) }

            is CreateQuestionSectionCommand.ToggleQuestion ->
                toggleQuestion(command.questionId, command.checked)

            CreateQuestionSectionCommand.CreateClicked -> create()
            CreateQuestionSectionCommand.Refresh -> onErrorRefreshClick()
        }
    }

    private fun onErrorRefreshClick() {
        when (_state.value) {
            is CreateQuestionSectionUiState.Content -> _state.value =
                (_state.value as CreateQuestionSectionUiState.Content).copy(fullScreenError = null)

            else -> {
                val quizId = currentQuizId ?: return
                init(quizId)
            }
        }
    }

    private fun init(quizId: Int) {
        currentQuizId = quizId
        _state.value = CreateQuestionSectionUiState.Loading

        viewModelScope.launch {
            when (val result = getQuestionsUseCase(null)) {

                is AppResult.Success -> {
                    _state.value = CreateQuestionSectionUiState.Content(
                        questions = result.data.map { it.domainToQuestionInQuestionSectionUiModel() },
                        titleQuery = "",
                        descriptionQuery = "",
                        orderIndex = 1,
                        pickCount = null,
                        selectedQuestionIds = emptySet(),
                        isCreating = false
                    )
                }

                is AppResult.Failure -> {
                    showUiError(result.error)
                }
            }
        }
    }

    private fun toggleQuestion(questionId: Int, checked: Boolean) {
        val s = _state.value as? CreateQuestionSectionUiState.Content ?: return
        if (s.isCreating) return

        val next =
            if (checked) s.selectedQuestionIds + questionId else s.selectedQuestionIds - questionId

        val fixedPickCount = if (next.isEmpty()) null else s.pickCount?.coerceIn(1, next.size)

        _state.value = s.copy(
            selectedQuestionIds = next,
            pickCount = fixedPickCount
        )
    }

    private fun create() {
        val quizId = currentQuizId ?: return
        val s = _state.value as? CreateQuestionSectionUiState.Content ?: return
        if (s.isCreating) return

        val titleOk = s.titleQuery.trim().isNotEmpty()
        val questionsOk = s.selectedQuestionIds.isNotEmpty()

        if (!titleOk || !questionsOk) {
            showUiError(
                AppError.Validation(
                    message = "Заполните название секции и выберите хотя бы 1 вопрос"
                )
            )
            return
        }

        val selectedCount = s.selectedQuestionIds.size
        val order = s.orderIndex.coerceIn(1, 100)
        val pick = s.pickCount?.coerceIn(1, selectedCount)

        _state.value = s.copy(isCreating = true)

        viewModelScope.launch {
            when (val result =
                createSectionUseCase(
                    quizId = quizId,
                    title = s.titleQuery.trim(),
                    description = s.descriptionQuery.trim().takeIf { it.isNotEmpty() },
                    orderIndex = order,
                    pickCount = pick,
                    questionIds = s.selectedQuestionIds.toList()
                )) {

                is AppResult.Success -> {
                    _events.tryEmit(CreateQuestionSectionEvent.Created)
                }

                is AppResult.Failure -> {
                    _state.value = s.copy(isCreating = false)
                    showUiError(result.error)
                }
            }
        }
    }

    private inline fun updateContent(block: (CreateQuestionSectionUiState.Content) -> CreateQuestionSectionUiState.Content) {
        val s = _state.value as? CreateQuestionSectionUiState.Content ?: return
        if (s.isCreating) return
        _state.value = block(s).normalize()
    }

    private fun CreateQuestionSectionUiState.Content.normalize(): CreateQuestionSectionUiState.Content {
        val selectedCount = selectedQuestionIds.size
        val fixedPick = if (selectedCount == 0) null else pickCount?.coerceIn(1, selectedCount)
        return copy(pickCount = fixedPick)
    }

    private fun showUiError(error: AppError) {
        when (val uiError = error.toScreenUiError()) {
            is UiError.SnackBar -> snackbarManager.show(uiError.message)

            is UiError.FullScreen -> {
                val current = _state.value
                if (current is CreateQuestionSectionUiState.Content) {
                    _state.value = current.copy(fullScreenError = uiError.model)
                } else {
                    _state.value = CreateQuestionSectionUiState.Error(uiError.model)
                }
            }
        }
    }
}

sealed interface CreateQuestionSectionCommand {
    data class Init(val quizId: Int) : CreateQuestionSectionCommand
    data object CloseClicked : CreateQuestionSectionCommand

    data class ChangeTitle(val value: String) : CreateQuestionSectionCommand
    data class ChangeDescription(val value: String) : CreateQuestionSectionCommand

    data class ChangeOrderIndex(val value: Int) : CreateQuestionSectionCommand
    data class ChangePickCount(val value: Int?) : CreateQuestionSectionCommand

    data class ToggleQuestion(val questionId: Int, val checked: Boolean) :
        CreateQuestionSectionCommand

    data object CreateClicked : CreateQuestionSectionCommand
    data object Refresh : CreateQuestionSectionCommand
}

sealed interface CreateQuestionSectionEvent {
    data object Close : CreateQuestionSectionEvent
    data object Created : CreateQuestionSectionEvent
}

sealed interface CreateQuestionSectionUiState {
    data object Initial : CreateQuestionSectionUiState
    data object Loading : CreateQuestionSectionUiState

    data class Content(
        val questions: List<QuestionInQuestionSectionUiModel>,
        val titleQuery: String,
        val descriptionQuery: String,
        val orderIndex: Int,
        val pickCount: Int?,
        val selectedQuestionIds: Set<Int>,
        val isCreating: Boolean,
        val fullScreenError: ErrorScreenModel? = null
    ) : CreateQuestionSectionUiState {
        val selectedCount: Int get() = selectedQuestionIds.size
        val canCreate: Boolean
            get() = titleQuery.trim().isNotEmpty() && selectedCount > 0 && !isCreating
    }

    data class Error(val model: ErrorScreenModel) : CreateQuestionSectionUiState
}