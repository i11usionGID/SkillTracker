package com.i11usion.feature.main.presentation.quiz.detail

import AppError
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.i11usion.core.domain.helpers.zip4
import com.i11usion.core.domain.model.network.AppResult
import com.i11usion.core.domain.model.quiz.QuizAnalytics
import com.i11usion.core.domain.usecase.quiz.DeleteAssignmentUseCase
import com.i11usion.core.domain.usecase.quiz.DeleteSectionUseCase
import com.i11usion.core.domain.usecase.quiz.GetQuestionSectionsUseCase
import com.i11usion.core.domain.usecase.quiz.GetQuizAnalyticsUseCase
import com.i11usion.core.domain.usecase.quiz.GetQuizAssignmentsUseCase
import com.i11usion.core.domain.usecase.quiz.GetQuizByIdUseCase
import com.i11usion.core.ui.model.ErrorScreenModel
import com.i11usion.core.ui.model.UiError
import com.i11usion.core.ui.model.UiText
import com.i11usion.feature.main.manager.SnackbarManager
import com.i11usion.feature.main.mapper.toScreenUiError
import com.i11usion.feature.main.presentation.quiz.detail.mapper.toUiModel
import com.i11usion.feature.main.presentation.quiz.detail.model.AssignmentDetailUiModel
import com.i11usion.feature.main.presentation.quiz.detail.model.QuestionSectionUiModel
import com.i11usion.feature.main.presentation.quiz.list.model.QuizManagerUiModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

class QuizDetailViewModel @Inject constructor(
    private val getQuizByIdUseCase: GetQuizByIdUseCase,
    private val getQuestionSectionsUseCase: GetQuestionSectionsUseCase,
    private val getQuizAssignmentsUseCase: GetQuizAssignmentsUseCase,
    private val getQuizAnalyticsUseCase: GetQuizAnalyticsUseCase,
    private val deleteSectionUseCase: DeleteSectionUseCase,
    private val deleteAssignmentUseCase: DeleteAssignmentUseCase,
    private val snackbarManager: SnackbarManager
) : ViewModel() {

    private companion object {
        const val RETRY_DELAY_MS = 10_000L
    }

    private var currentQuizId: Int? = null
    private var initJob: Job? = null

    private val _state = MutableStateFlow<QuizDetailUiState>(QuizDetailUiState.Initial)
    val state = _state.asStateFlow()

    fun processCommand(command: QuizDetailCommand) {
        when (command) {
            is QuizDetailCommand.Init -> init(command.quizId)
            is QuizDetailCommand.SelectTab -> selectTab(command.tab)
            is QuizDetailCommand.DeleteSection -> deleteSection(command.sectionId)
            is QuizDetailCommand.DeleteAssignment -> deleteAssignment(command.assignmentId)
            QuizDetailCommand.Refresh -> onErrorRefreshClick()
        }
    }

    private fun onErrorRefreshClick() {
        when (_state.value) {
            is QuizDetailUiState.Content -> _state.value =
                (_state.value as QuizDetailUiState.Content).copy(fullScreenError = null)

            else -> {
                val quizId = currentQuizId ?: return
                init(quizId)
            }
        }
    }

    private fun init(quizId: Int) {
        val previousTab = (_state.value as? QuizDetailUiState.Content)?.selectedTab
            ?: QuizDetailTab.SECTIONS

        currentQuizId = quizId
        initJob?.cancel()
        _state.value = QuizDetailUiState.Loading

        initJob = viewModelScope.launch {
            val quizDeferred = async { retryRequest { getQuizByIdUseCase(quizId) } }
            val sectionsDeferred = async { retryRequest { getQuestionSectionsUseCase(quizId) } }
            val assignmentsDeferred = async { retryRequest { getQuizAssignmentsUseCase(quizId) } }
            val analyticsDeferred = async { retryRequest { getQuizAnalyticsUseCase(quizId) } }

            val combined = quizDeferred.await().zip4(
                sectionsDeferred.await(),
                assignmentsDeferred.await(),
                analyticsDeferred.await()
            ) { quiz, sections, assignments, analytics ->
                QuizDetailUiState.Content(
                    quiz = quiz.toUiModel(),
                    selectedTab = previousTab,
                    sections = sections.map { it.toUiModel() },
                    assignments = assignments.map { it.toUiModel() },
                    analytics = analytics,
                    isDeleting = false,
                    fullScreenError = null
                )
            }

            when (combined) {
                is AppResult.Success -> _state.value = combined.data
                is AppResult.Failure -> showUiError(combined.error)
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

    private fun selectTab(tab: QuizDetailTab) {
        val current = _state.value
        if (current !is QuizDetailUiState.Content) return
        if (current.selectedTab == tab) return
        _state.value = current.copy(selectedTab = tab)
    }

    private fun deleteSection(sectionId: Int) {
        val quizId = currentQuizId ?: return
        val current = _state.value as? QuizDetailUiState.Content ?: return
        if (current.isDeleting) return

        _state.value = current.copy(isDeleting = true)

        viewModelScope.launch {
            when (val deleteRes = deleteSectionUseCase(quizId = quizId, sectionId = sectionId)) {
                is AppResult.Success -> {
                    when (val sectionsRes = retryRequest { getQuestionSectionsUseCase(quizId) }) {
                        is AppResult.Success -> {
                            val s = _state.value as? QuizDetailUiState.Content ?: return@launch
                            _state.value = s.copy(
                                sections = sectionsRes.data.map { it.toUiModel() },
                                isDeleting = false
                            )
                        }

                        is AppResult.Failure -> {
                            stopDeleting()
                            showUiError(sectionsRes.error)
                        }
                    }
                }

                is AppResult.Failure -> {
                    stopDeleting()
                    showUiError(deleteRes.error)
                }
            }
        }
    }

    private fun deleteAssignment(assignmentId: Int) {
        val quizId = currentQuizId ?: return
        val current = _state.value as? QuizDetailUiState.Content ?: return
        if (current.isDeleting) return

        _state.value = current.copy(isDeleting = true)

        viewModelScope.launch {
            when (val deleteRes = deleteAssignmentUseCase(assignmentId)) {
                is AppResult.Success -> {
                    when (val assignRes = retryRequest { getQuizAssignmentsUseCase(quizId) }) {
                        is AppResult.Success -> {
                            val s = _state.value as? QuizDetailUiState.Content ?: return@launch
                            _state.value = s.copy(
                                assignments = assignRes.data.map { it.toUiModel() },
                                isDeleting = false
                            )
                        }

                        is AppResult.Failure -> {
                            stopDeleting()
                            showUiError(assignRes.error)
                        }
                    }
                }

                is AppResult.Failure -> {
                    stopDeleting()
                    showUiError(deleteRes.error)
                }
            }
        }
    }

    private fun stopDeleting() {
        val s = _state.value as? QuizDetailUiState.Content ?: return
        _state.value = s.copy(isDeleting = false)
    }

    private fun showUiError(error: AppError) {
        when (val uiError = error.toScreenUiError()) {
            is UiError.SnackBar -> snackbarManager.show(uiError.message)

            is UiError.FullScreen -> {
                val current = _state.value
                if (current is QuizDetailUiState.Content) {
                    _state.value = current.copy(fullScreenError = uiError.model)
                } else {
                    _state.value = QuizDetailUiState.Error(uiError.model)
                }
            }
        }
    }
}


sealed interface QuizDetailCommand {
    data class Init(val quizId: Int) : QuizDetailCommand
    data class SelectTab(val tab: QuizDetailTab) : QuizDetailCommand

    data class DeleteSection(val sectionId: Int) : QuizDetailCommand
    data class DeleteAssignment(val assignmentId: Int) : QuizDetailCommand
    data object Refresh : QuizDetailCommand
}


sealed interface QuizDetailUiState {
    data object Initial : QuizDetailUiState
    data object Loading : QuizDetailUiState

    data class Content(
        val quiz: QuizManagerUiModel,
        val selectedTab: QuizDetailTab,
        val sections: List<QuestionSectionUiModel>,
        val assignments: List<AssignmentDetailUiModel>,
        val analytics: QuizAnalytics,
        val isDeleting: Boolean = false,
        val fullScreenError: ErrorScreenModel? = null
    ) : QuizDetailUiState


    data class Error(val model: ErrorScreenModel) : QuizDetailUiState
}
