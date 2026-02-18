package com.i11usion.feature.main.presentation.quiz.detail.question_section.create

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.i11usion.core.domain.usecase.quiz.CreateQuestionSectionUseCase
import com.i11usion.core.domain.usecase.quiz.GetQuestionsUseCase
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
        }
    }

    private fun init(quizId: Int) {
        currentQuizId = quizId
        _state.value = CreateQuestionSectionUiState.Loading

        viewModelScope.launch {
            runCatching { getQuestionsUseCase(null) }
                .onSuccess { questions ->
                    _state.value = CreateQuestionSectionUiState.Content(
                        questions = questions.map { it.domainToQuestionInQuestionSectionUiModel() },
                        titleQuery = "",
                        descriptionQuery = "",
                        orderIndex = 1,
                        pickCount = null,
                        selectedQuestionIds = emptySet(),
                        isCreating = false
                    )
                }
                .onFailure { t ->
                    _state.value =
                        CreateQuestionSectionUiState.Error(
                            t.message ?: "Не удалось загрузить вопросы"
                        )
                }
        }
    }

    private fun toggleQuestion(questionId: Int, checked: Boolean) {
        val s = _state.value as? CreateQuestionSectionUiState.Content ?: return
        if (s.isCreating) return

        val next =
            if (checked) s.selectedQuestionIds + questionId else s.selectedQuestionIds - questionId

        // pickCount не может быть больше выбранных вопросов (и null если ничего не выбрано)
        val fixedPickCount = if (next.isEmpty()) null else s.pickCount?.coerceIn(1, next.size)

        _state.value = s.copy(
            selectedQuestionIds = next,
            pickCount = fixedPickCount
            // ✅ orderIndex НЕ меняем
        )
    }

    private fun create() {
        val quizId = currentQuizId ?: return
        val s = _state.value as? CreateQuestionSectionUiState.Content ?: return
        if (s.isCreating) return

        val titleOk = s.titleQuery.trim().isNotEmpty()
        val questionsOk = s.selectedQuestionIds.isNotEmpty()

        if (!titleOk || !questionsOk) {
            _events.tryEmit(
                CreateQuestionSectionEvent.ShowError(
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
            runCatching {
                createSectionUseCase(
                    quizId = quizId,
                    title = s.titleQuery.trim(),
                    description = s.descriptionQuery.trim().takeIf { it.isNotEmpty() },
                    orderIndex = order,
                    pickCount = pick,
                    questionIds = s.selectedQuestionIds.toList()
                )
            }.onSuccess {
                _events.tryEmit(CreateQuestionSectionEvent.Created)
            }.onFailure { t ->
                val cur = _state.value as? CreateQuestionSectionUiState.Content
                if (cur != null) _state.value = cur.copy(isCreating = false)
                _events.tryEmit(
                    CreateQuestionSectionEvent.ShowError(
                        message = t.message ?: "Не удалось создать секцию"
                    )
                )
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
        return copy(pickCount = fixedPick) // ✅ orderIndex не трогаем
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
}

sealed interface CreateQuestionSectionEvent {
    data object Close : CreateQuestionSectionEvent
    data object Created : CreateQuestionSectionEvent
    data class ShowError(val message: String) : CreateQuestionSectionEvent
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
        val isCreating: Boolean
    ) : CreateQuestionSectionUiState {
        val selectedCount: Int get() = selectedQuestionIds.size
        val canCreate: Boolean
            get() = titleQuery.trim().isNotEmpty() && selectedCount > 0 && !isCreating
    }

    data class Error(val message: String) : CreateQuestionSectionUiState
}