package com.i11usion.feature.main.presentation.quiz.detail.question_section.update

import AppError
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.i11usion.core.domain.helpers.zip
import com.i11usion.core.domain.model.network.AppResult
import com.i11usion.core.domain.usecase.quiz.GetQuestionSectionByIdUseCase
import com.i11usion.core.domain.usecase.quiz.GetQuestionsUseCase
import com.i11usion.core.domain.usecase.quiz.UpdateQuestionSectionUseCase
import com.i11usion.core.ui.model.ErrorScreenModel
import com.i11usion.core.ui.model.UiError
import com.i11usion.feature.main.manager.SnackbarManager
import com.i11usion.feature.main.mapper.toScreenUiError
import com.i11usion.feature.main.presentation.quiz.detail.question_section.create.mapper.domainToQuestionInQuestionSectionUiModel
import com.i11usion.feature.main.presentation.quiz.detail.question_section.create.model.QuestionInQuestionSectionUiModel
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

class UpdateQuestionSectionViewModel @Inject constructor(
    private val getQuestionsUseCase: GetQuestionsUseCase,
    private val getQuestionSectionByIdUseCase: GetQuestionSectionByIdUseCase,
    private val updateQuestionSectionUseCase: UpdateQuestionSectionUseCase,
    private val snackbarManager: SnackbarManager
) : ViewModel() {

    private var currentQuizId: Int? = null
    private var currentSectionId: Int? = null

    private val _state =
        MutableStateFlow<UpdateQuestionSectionUiState>(UpdateQuestionSectionUiState.Initial)
    val state = _state.asStateFlow()

    private val _events = MutableSharedFlow<UpdateQuestionSectionEvent>(extraBufferCapacity = 1)
    val events = _events.asSharedFlow()

    fun processCommand(command: UpdateQuestionSectionCommand) {
        when (command) {
            is UpdateQuestionSectionCommand.Init -> init(
                quizId = command.quizId,
                sectionId = command.sectionId
            )

            UpdateQuestionSectionCommand.CloseClicked -> _events.tryEmit(UpdateQuestionSectionEvent.Close)

            is UpdateQuestionSectionCommand.ChangeTitle ->
                updateContent { it.copy(titleQuery = command.value) }

            is UpdateQuestionSectionCommand.ChangeDescription ->
                updateContent { it.copy(descriptionQuery = command.value) }

            is UpdateQuestionSectionCommand.ChangeOrderIndex ->
                updateContent { it.copy(orderIndex = command.value.coerceIn(1, 100)) }

            is UpdateQuestionSectionCommand.ChangePickCount ->
                updateContent { it.copy(pickCount = command.value) }

            is UpdateQuestionSectionCommand.ToggleQuestion ->
                toggleQuestion(command.questionId, command.checked)

            UpdateQuestionSectionCommand.SaveClicked -> save()
            UpdateQuestionSectionCommand.Refresh -> onErrorRefreshClick()
        }
    }

    private fun onErrorRefreshClick() {
        when (_state.value) {
            is UpdateQuestionSectionUiState.Content -> _state.value =
                (_state.value as UpdateQuestionSectionUiState.Content).copy(fullScreenError = null)

            else -> {
                val quizId = currentQuizId ?: return
                val sectionId = currentSectionId ?: return
                init(
                    quizId = quizId,
                    sectionId = sectionId
                )
            }
        }
    }

    private fun init(quizId: Int, sectionId: Int) {
        if (currentQuizId == quizId && currentSectionId == sectionId && _state.value is UpdateQuestionSectionUiState.Content) return
        currentQuizId = quizId
        currentSectionId = sectionId
        _state.value = UpdateQuestionSectionUiState.Loading

        viewModelScope.launch {
            val questionsDeferred = async {
                getQuestionsUseCase()
            }
            val sectionDeferred = async {
                getQuestionSectionByIdUseCase(quizId = quizId, questionSectionId = sectionId)
            }

            val result = questionsDeferred.await()
                .zip(sectionDeferred.await()) { question, section -> question to section }

            when (result) {

                is AppResult.Success -> {
                    val questions = result.data.first
                    val section = result.data.second
                    val content = UpdateQuestionSectionUiState.Content(
                        questions = questions.map { it.domainToQuestionInQuestionSectionUiModel() },
                        titleQuery = section.title,
                        descriptionQuery = section.description ?: "",
                        orderIndex = section.orderIndex.coerceIn(1, 100),
                        pickCount = section.pickCount,
                        selectedQuestionIds = section.questions.map {
                            it.id
                        }.toSet(),
                        isSaving = false
                    ).normalize()

                    _state.value = content
                }

                is AppResult.Failure -> {
                    showUiError(result.error)
                }
            }
        }
    }

    private fun toggleQuestion(questionId: Int, checked: Boolean) {
        val s = _state.value as? UpdateQuestionSectionUiState.Content ?: return
        if (s.isSaving) return

        val next =
            if (checked) s.selectedQuestionIds + questionId else s.selectedQuestionIds - questionId

        val fixedPickCount = if (next.isEmpty()) null else s.pickCount?.coerceIn(1, next.size)

        _state.value = s.copy(
            selectedQuestionIds = next,
            pickCount = fixedPickCount
        )
    }

    private fun save() {
        val quizId = currentQuizId ?: return
        val sectionId = currentSectionId ?: return
        val s = _state.value as? UpdateQuestionSectionUiState.Content ?: return
        if (s.isSaving) return

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

        _state.value = s.copy(isSaving = true)

        viewModelScope.launch {
            when (val result = updateQuestionSectionUseCase(
                quizId = quizId,
                questionSectionId = sectionId,
                title = s.titleQuery.trim(),
                description = s.descriptionQuery.trim().takeIf { it.isNotEmpty() },
                orderIndex = order,
                pickCount = pick,
                questionIds = s.selectedQuestionIds.toList()
            )) {

                is AppResult.Success -> {
                    _events.tryEmit(UpdateQuestionSectionEvent.Saved)
                }

                is AppResult.Failure -> {
                    val cur = _state.value as? UpdateQuestionSectionUiState.Content
                    if (cur != null) _state.value = cur.copy(isSaving = false)
                    showUiError(result.error)
                }
            }
        }
    }

    private inline fun updateContent(block: (UpdateQuestionSectionUiState.Content) -> UpdateQuestionSectionUiState.Content) {
        val s = _state.value as? UpdateQuestionSectionUiState.Content ?: return
        if (s.isSaving) return
        _state.value = block(s).normalize()
    }

    private fun UpdateQuestionSectionUiState.Content.normalize(): UpdateQuestionSectionUiState.Content {
        val selectedCount = selectedQuestionIds.size
        val fixedPick = if (selectedCount == 0) null else pickCount?.coerceIn(1, selectedCount)
        return copy(pickCount = fixedPick)
    }

    private fun showUiError(error: AppError) {
        when (val uiError = error.toScreenUiError()) {
            is UiError.SnackBar -> snackbarManager.show(uiError.message)

            is UiError.FullScreen -> {
                val current = _state.value
                if (current is UpdateQuestionSectionUiState.Content) {
                    _state.value = current.copy(fullScreenError = uiError.model)
                } else {
                    _state.value = UpdateQuestionSectionUiState.Error(uiError.model)
                }
            }
        }
    }
}

sealed interface UpdateQuestionSectionCommand {
    data class Init(val quizId: Int, val sectionId: Int) : UpdateQuestionSectionCommand
    data object CloseClicked : UpdateQuestionSectionCommand

    data class ChangeTitle(val value: String) : UpdateQuestionSectionCommand
    data class ChangeDescription(val value: String) : UpdateQuestionSectionCommand

    data class ChangeOrderIndex(val value: Int) : UpdateQuestionSectionCommand
    data class ChangePickCount(val value: Int?) : UpdateQuestionSectionCommand

    data class ToggleQuestion(val questionId: Int, val checked: Boolean) :
        UpdateQuestionSectionCommand

    data object SaveClicked : UpdateQuestionSectionCommand
    data object Refresh : UpdateQuestionSectionCommand
}

sealed interface UpdateQuestionSectionEvent {
    data object Close : UpdateQuestionSectionEvent
    data object Saved : UpdateQuestionSectionEvent
}

sealed interface UpdateQuestionSectionUiState {
    data object Initial : UpdateQuestionSectionUiState
    data object Loading : UpdateQuestionSectionUiState

    data class Content(
        val questions: List<QuestionInQuestionSectionUiModel>,
        val titleQuery: String,
        val descriptionQuery: String,
        val orderIndex: Int,
        val pickCount: Int?,
        val selectedQuestionIds: Set<Int>,
        val isSaving: Boolean,
        val fullScreenError: ErrorScreenModel? = null
    ) : UpdateQuestionSectionUiState {
        val selectedCount: Int get() = selectedQuestionIds.size
        val canSave: Boolean
            get() = titleQuery.trim().isNotEmpty() && selectedCount > 0 && !isSaving
    }

    data class Error(val model: ErrorScreenModel) : UpdateQuestionSectionUiState
}