package com.i11usion.feature.main.presentation.quiz.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.i11usion.core.domain.model.quiz.AssignmentDetail
import com.i11usion.core.domain.model.quiz.QuestionSection
import com.i11usion.core.domain.model.quiz.Quiz
import com.i11usion.core.domain.model.quiz.QuizAnalytics
import com.i11usion.core.domain.usecase.quiz.DeleteAssignmentUseCase
import com.i11usion.core.domain.usecase.quiz.DeleteSectionUseCase
import com.i11usion.core.domain.usecase.quiz.GetQuestionSectionsUseCase
import com.i11usion.core.domain.usecase.quiz.GetQuizAnalyticsUseCase
import com.i11usion.core.domain.usecase.quiz.GetQuizAssignmentsUseCase
import com.i11usion.core.domain.usecase.quiz.GetQuizByIdUseCase
import com.i11usion.feature.main.presentation.quiz.detail.mapper.toUiModel
import com.i11usion.feature.main.presentation.quiz.detail.model.AssignmentDetailUiModel
import com.i11usion.feature.main.presentation.quiz.detail.model.QuestionSectionUiModel
import com.i11usion.feature.main.presentation.quiz.list.model.QuizManagerUiModel
import kotlinx.coroutines.async
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
    private val deleteAssignmentUseCase: DeleteAssignmentUseCase
) : ViewModel() {

    private var currentQuizId: Int? = null

    private val _state = MutableStateFlow<QuizDetailUiState>(QuizDetailUiState.Initial)
    val state = _state.asStateFlow()

    fun processCommand(command: QuizDetailCommand) {
        when (command) {
            is QuizDetailCommand.Init -> init(command.quizId)
            is QuizDetailCommand.SelectTab -> selectTab(command.tab)
            is QuizDetailCommand.DeleteSection -> deleteSection(command.sectionId)
            is QuizDetailCommand.DeleteAssignment -> deleteAssignment(command.assignmentId)
        }
    }

    private fun init(quizId: Int) {
        currentQuizId = quizId
        _state.value = QuizDetailUiState.Loading

        viewModelScope.launch {
            runCatching {
                val quizDeferred = async { loadQuiz(quizId) }
                val sectionsDeferred = async { loadSections(quizId) }
                val assignmentsDeferred = async { loadAssignments(quizId) }
                val analyticsDeferred = async { loadAnalytics(quizId) }

                QuizDetailUiState.Content(
                    quiz = quizDeferred.await(),
                    selectedTab = QuizDetailTab.SECTIONS,
                    sections = sectionsDeferred.await(),
                    assignments = assignmentsDeferred.await(),
                    analytics = analyticsDeferred.await(),
                    isDeleting = false
                )
            }.onSuccess { content ->
                _state.value = content
            }.onFailure { t ->
                _state.value = QuizDetailUiState.Error(t.message ?: "Unknown error")
            }
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
            runCatching {
                deleteSectionUseCase(quizId = quizId, sectionId = sectionId)
                loadSections(quizId)
            }.onSuccess { newSections ->
                val s = _state.value as? QuizDetailUiState.Content ?: return@onSuccess
                _state.value = s.copy(sections = newSections, isDeleting = false)
            }.onFailure { t ->
                val s = _state.value as? QuizDetailUiState.Content
                if (s != null) _state.value = s.copy(isDeleting = false)
                else _state.value =
                    QuizDetailUiState.Error(t.message ?: "Не удалось удалить секцию")
            }
        }
    }

    private fun deleteAssignment(assignmentId: Int) {
        val quizId = currentQuizId ?: return
        val current = _state.value as? QuizDetailUiState.Content ?: return
        if (current.isDeleting) return

        _state.value = current.copy(isDeleting = true)

        viewModelScope.launch {
            runCatching {
                deleteAssignmentUseCase(assignmentId)
                loadAssignments(quizId)
            }.onSuccess { newAssignments ->
                val s = _state.value as? QuizDetailUiState.Content ?: return@onSuccess
                _state.value = s.copy(assignments = newAssignments, isDeleting = false)
            }.onFailure { t ->
                val s = _state.value as? QuizDetailUiState.Content
                if (s != null) _state.value = s.copy(isDeleting = false)
                else _state.value =
                    QuizDetailUiState.Error(t.message ?: "Не удалось удалить назначение")
            }
        }
    }

    private suspend fun loadQuiz(quizId: Int): QuizManagerUiModel {
        val quiz: Quiz = getQuizByIdUseCase(quizId)
        return quiz.toUiModel()
    }

    private suspend fun loadSections(quizId: Int): List<QuestionSectionUiModel> {
        val sections: List<QuestionSection> = getQuestionSectionsUseCase(quizId)
        return sections.map { it.toUiModel() }
    }

    private suspend fun loadAssignments(quizId: Int): List<AssignmentDetailUiModel> {
        val list: List<AssignmentDetail> = getQuizAssignmentsUseCase(quizId)
        return list.map { it.toUiModel() }
    }

    private suspend fun loadAnalytics(quizId: Int): QuizAnalytics {
        return getQuizAnalyticsUseCase(quizId)
    }
}

sealed interface QuizDetailCommand {
    data class Init(val quizId: Int) : QuizDetailCommand
    data class SelectTab(val tab: QuizDetailTab) : QuizDetailCommand

    data class DeleteSection(val sectionId: Int) : QuizDetailCommand
    data class DeleteAssignment(val assignmentId: Int) : QuizDetailCommand
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
        val isDeleting: Boolean = false
    ) : QuizDetailUiState


    data class Error(val message: String) : QuizDetailUiState
}
