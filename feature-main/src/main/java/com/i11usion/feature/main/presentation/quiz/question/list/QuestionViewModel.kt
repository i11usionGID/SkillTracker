package com.i11usion.feature.main.presentation.quiz.question.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.i11usion.core.domain.model.quiz.QuestionType
import com.i11usion.core.domain.usecase.quiz.DeleteQuestionUseCase
import com.i11usion.core.domain.usecase.quiz.GetQuestionsUseCase
import com.i11usion.feature.main.presentation.quiz.question.list.mapper.domainToUi
import com.i11usion.feature.main.presentation.quiz.question.list.model.QuestionUiModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

class QuestionViewModel @Inject constructor(
    private val getQuestionsUseCase: GetQuestionsUseCase,
    private val deleteQuestionUseCase: DeleteQuestionUseCase
) : ViewModel() {

    private val _state = MutableStateFlow<QuestionUiState>(QuestionUiState.Initial)
    val state = _state.asStateFlow()

    init {
        loadQuestions(QuestionFilter.ALL)
    }

    fun processCommand(command: QuestionCommand) {
        when (command) {
            is QuestionCommand.SelectFilter -> loadQuestions(command.filter)
            is QuestionCommand.DeleteQuestion -> deleteQuestion(command.questionId)
            QuestionCommand.Refresh -> {
                val currentFilter =
                    (_state.value as? QuestionUiState.Content)?.selectedFilter ?: QuestionFilter.ALL
                loadQuestions(currentFilter)
            }
        }
    }

    private fun loadQuestions(filter: QuestionFilter) {
        viewModelScope.launch {
            _state.update { current ->
                val content = current as? QuestionUiState.Content
                (content ?: QuestionUiState.Content()).copy(
                    selectedFilter = filter,
                    isLoading = true
                )
            }

            try {
                val domain = getQuestionsUseCase(questionType = filter.type)
                val uiList: List<QuestionUiModel> = domain.map { it.domainToUi() }

                _state.update { current ->
                    val content = current as? QuestionUiState.Content
                    (content ?: QuestionUiState.Content()).copy(
                        selectedFilter = filter,
                        questionList = uiList,
                        isLoading = false
                    )
                }
            } catch (t: Throwable) {
                _state.value = QuestionUiState.Error(t.message ?: "Failed to load questions")
            }
        }
    }

    private fun deleteQuestion(questionId: Int) {
        viewModelScope.launch {
            val content = _state.value as? QuestionUiState.Content ?: return@launch
            val oldList = content.questionList

            _state.value = content.copy(
                questionList = oldList.filterNot { it.id == questionId }
            )

            try {
                deleteQuestionUseCase(questionId)
            } catch (t: Throwable) {
                _state.value = content.copy(questionList = oldList)
            }
        }
    }
}

sealed interface QuestionCommand {
    data object Refresh : QuestionCommand
    data class SelectFilter(val filter: QuestionFilter) : QuestionCommand
    data class DeleteQuestion(val questionId: Int) : QuestionCommand
}

enum class QuestionFilter(val title: String, val type: QuestionType?) {
    ALL("Все", null),
    SINGLE("Одиночный выбор", QuestionType.SINGLE),
    MULTIPLE("Множественный выбор", QuestionType.MULTIPLE),
    TEXT("Текстовый ответ", QuestionType.TEXT),
}

sealed interface QuestionUiState {
    data object Initial : QuestionUiState

    data class Content(
        val selectedFilter: QuestionFilter = QuestionFilter.ALL,
        val questionList: List<QuestionUiModel> = emptyList(),
        val isLoading: Boolean = false,
    ) : QuestionUiState

    data class Error(val message: String) : QuestionUiState
}

