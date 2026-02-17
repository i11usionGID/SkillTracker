package com.i11usion.feature.main.presentation.quiz.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.i11usion.core.domain.model.quiz.QuizAnalytics
import com.i11usion.feature.main.presentation.quiz.detail.model.AssignmentDetailUiModel
import com.i11usion.feature.main.presentation.quiz.detail.model.QuizSectionUiModel
import com.i11usion.feature.main.presentation.quiz.list.model.QuizManagerUiModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

class QuizDetailViewModel @Inject constructor(
//    private val getQuizByIdUseCase: Any,
//    private val getSections: Any,
//    private val getAssignments: Any,
//    private val getUserById: Any,
//    private val getQuizAnalytics: Any,
//    private val deleteSection: Any,
//    private val deleteAssignment: Any,
) : ViewModel() {

    private var currentQuizId: Int? = null

    private val _state = MutableStateFlow<QuizDetailUiState>(QuizDetailUiState.Initial)
    val state = _state.asStateFlow()

    private val _events = MutableSharedFlow<QuizDetailEvent>()
    val events = _events.asSharedFlow()

    fun processCommand(command: QuizDetailCommand) {
        when (command) {
            is QuizDetailCommand.Init -> init(command.quizId)
            is QuizDetailCommand.SelectTab -> selectTab(command.tab)
        }
    }

    private fun init(quizId: Int) {
        currentQuizId = quizId

        _state.value = QuizDetailUiState.Loading

        viewModelScope.launch {
            try {
                // TODO: заменить на реальные вызовы usecase + маппинг в UiModel
                val quiz: QuizManagerUiModel = loadQuiz(quizId)
                val sections: List<QuizSectionUiModel> = loadSections(quizId)
                val assignments: List<AssignmentDetailUiModel> = loadAssignments(quizId)
                val analytics: QuizAnalytics = loadAnalytics(quizId)

                _state.value = QuizDetailUiState.Content(
                    quiz = quiz,
                    selectedTab = QuizDetailTab.SECTIONS,
                    sections = sections,
                    assignments = assignments,
                    analytics = analytics
                )
            } catch (t: Throwable) {
                _state.value = QuizDetailUiState.Error(
                    message = t.message ?: "Unknown error"
                )
            }
        }
    }

    private fun selectTab(tab: QuizDetailTab) {
        val current = _state.value
        if (current !is QuizDetailUiState.Content) return

        if (current.selectedTab == tab) return

        _state.value = current.copy(selectedTab = tab)

    }

    private suspend fun loadQuiz(quizId: Int): QuizManagerUiModel {
        // TODO getQuizByIdUseCase(...)
        throw NotImplementedError("loadQuiz not implemented")
    }

    private suspend fun loadSections(quizId: Int): List<QuizSectionUiModel> {
        // TODO getSections(...)
        throw NotImplementedError("loadSections not implemented")
    }

    private suspend fun loadAssignments(quizId: Int): List<AssignmentDetailUiModel> {
        // TODO getAssignments(...)
        throw NotImplementedError("loadAssignments not implemented")
    }

    private suspend fun loadAnalytics(quizId: Int): QuizAnalytics {
        // TODO getQuizAnalytics(...)
        throw NotImplementedError("loadAnalytics not implemented")
    }
}

sealed interface QuizDetailEvent {
    data object NavigateBack : QuizDetailEvent
}

sealed interface QuizDetailCommand {
    data class Init(val quizId: Int) : QuizDetailCommand
    data class SelectTab(val tab: QuizDetailTab) : QuizDetailCommand
}


sealed interface QuizDetailUiState {
    data object Initial : QuizDetailUiState
    data object Loading : QuizDetailUiState

    data class Content(
        val quiz: QuizManagerUiModel,
        val selectedTab: QuizDetailTab,
        val sections: List<QuizSectionUiModel>,
        val assignments: List<AssignmentDetailUiModel>,
        val analytics: QuizAnalytics
    ) : QuizDetailUiState

    data class Error(val message: String) : QuizDetailUiState
}
