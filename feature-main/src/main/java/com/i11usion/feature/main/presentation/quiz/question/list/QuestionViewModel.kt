package com.i11usion.feature.main.presentation.quiz.question.list

import AppError
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.i11usion.core.domain.model.network.AppResult
import com.i11usion.core.domain.model.quiz.QuestionType
import com.i11usion.core.domain.usecase.quiz.DeleteQuestionUseCase
import com.i11usion.core.domain.usecase.quiz.GetQuestionsUseCase
import com.i11usion.core.ui.model.ErrorScreenModel
import com.i11usion.core.ui.model.UiError
import com.i11usion.core.ui.model.UiText
import com.i11usion.feature.main.manager.SnackbarManager
import com.i11usion.feature.main.mapper.toScreenUiError
import com.i11usion.feature.main.presentation.quiz.question.list.mapper.domainToUi
import com.i11usion.feature.main.presentation.quiz.question.list.model.QuestionUiModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

class QuestionViewModel @Inject constructor(
    private val getQuestionsUseCase: GetQuestionsUseCase,
    private val deleteQuestionUseCase: DeleteQuestionUseCase,
    private val snackbarManager: SnackbarManager
) : ViewModel() {

    private companion object {
        const val RETRY_DELAY_MS = 10_000L
    }

    private val _state = MutableStateFlow<QuestionUiState>(QuestionUiState.Initial)
    val state = _state.asStateFlow()

    private var isFirstLoading: Boolean = true
    private var loadJob: Job? = null

    init {
        loadQuestions(QuestionFilter.ALL)
    }

    fun processCommand(command: QuestionCommand) {
        when (command) {
            is QuestionCommand.SelectFilter -> loadQuestions(command.filter)
            is QuestionCommand.DeleteQuestion -> deleteQuestion(command.questionId)
            QuestionCommand.Refresh -> {
                if (isFirstLoading) {
                    isFirstLoading = false
                    return
                } else {
                    val currentFilter =
                        (_state.value as? QuestionUiState.Content)?.selectedFilter
                            ?: QuestionFilter.ALL
                    loadQuestions(currentFilter)
                }
            }
        }
    }

    private fun loadQuestions(filter: QuestionFilter) {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            _state.update { current ->
                val content = current as? QuestionUiState.Content
                (content ?: QuestionUiState.Content()).copy(
                    selectedFilter = filter,
                    isLoading = true,
                    fullScreenError = null
                )
            }

            when (val result = retryRequest { getQuestionsUseCase(questionType = filter.type) }) {
                is AppResult.Success -> {
                    val uiList: List<QuestionUiModel> = result.data.map { it.domainToUi() }

                    _state.update { current ->
                        val content = current as? QuestionUiState.Content
                        (content ?: QuestionUiState.Content()).copy(
                            selectedFilter = filter,
                            questionList = uiList,
                            isLoading = false,
                            fullScreenError = null
                        )
                    }
                }

                is AppResult.Failure -> {
                    _state.update { current ->
                        val content = current as? QuestionUiState.Content
                        (content ?: QuestionUiState.Content()).copy(
                            selectedFilter = filter,
                            isLoading = false
                        )
                    }
                    showUiError(result.error)
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

    private fun deleteQuestion(questionId: Int) {
        viewModelScope.launch {
            when (val result = deleteQuestionUseCase(questionId)) {
                is AppResult.Success -> {
                    val content = _state.value as? QuestionUiState.Content ?: return@launch
                    val oldList = content.questionList

                    _state.value = content.copy(
                        questionList = oldList.filterNot { it.id == questionId }
                    )
                }

                is AppResult.Failure -> {
                    showUiError(result.error)
                }
            }
        }
    }

    private fun showUiError(error: AppError) {
        when (val uiError = error.toScreenUiError()) {
            is UiError.SnackBar -> snackbarManager.show(uiError.message)

            is UiError.FullScreen -> {
                val current = _state.value
                if (current is QuestionUiState.Content) {
                    _state.value = current.copy(fullScreenError = uiError.model)
                } else {
                    _state.value = QuestionUiState.Error(uiError.model)
                }
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
        val fullScreenError: ErrorScreenModel? = null
    ) : QuestionUiState

    data class Error(val model: ErrorScreenModel) : QuestionUiState
}

