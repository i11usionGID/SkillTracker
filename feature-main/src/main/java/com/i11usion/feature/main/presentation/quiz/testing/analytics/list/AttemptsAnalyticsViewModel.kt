package com.i11usion.feature.main.presentation.quiz.testing.analytics.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.i11usion.core.domain.model.user.User
import com.i11usion.core.domain.usecase.quiz.attempt.GetQuizAttemptsAnalyticsUseCase
import com.i11usion.core.domain.usecase.user.GetUsersListUseCase
import com.i11usion.feature.main.presentation.quiz.testing.analytics.list.mapper.toUiModel
import com.i11usion.feature.main.presentation.quiz.testing.analytics.list.model.AttemptAnalyticsUiModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

class AttemptsAnalyticsViewModel @Inject constructor(
    private val getUsersListUseCase: GetUsersListUseCase,
    private val getQuizAttemptsAnalyticsUseCase: GetQuizAttemptsAnalyticsUseCase
) : ViewModel() {

    private var currentQuizId: Int? = null

    private val _state =
        MutableStateFlow<AttemptsAnalyticsUiState>(AttemptsAnalyticsUiState.Initial)
    val state = _state.asStateFlow()

    private val _events = MutableSharedFlow<AttemptsAnalyticsEvent>(extraBufferCapacity = 1)
    val events = _events.asSharedFlow()

    fun processCommand(command: AttemptsAnalyticsCommand) {
        when (command) {
            is AttemptsAnalyticsCommand.Init -> init(command.quizId)

            is AttemptsAnalyticsCommand.EmployeeSelected -> {
                val quizId = currentQuizId ?: return
                updateSelectedEmployee(command.employeeId)
                loadAttempts(quizId, command.employeeId)
            }

            AttemptsAnalyticsCommand.FinishClicked -> {
                _events.tryEmit(AttemptsAnalyticsEvent.Finish)
            }
        }
    }

    private fun init(quizId: Int) {
        if (currentQuizId == quizId && _state.value is AttemptsAnalyticsUiState.Content) return
        currentQuizId = quizId

        _state.value = AttemptsAnalyticsUiState.Content(
            users = emptyList(),
            attempts = emptyList(),
            selectedEmployeeId = null,
            isAttemptsListLoading = true
        )

        loadEmployees()
        loadAttempts(quizId, employeeId = null)
    }

    private fun loadEmployees() {
        viewModelScope.launch {
            runCatching {
                getUsersListUseCase(role = "employee")
            }.onSuccess { users ->
                _state.update { s ->
                    val content = (s as? AttemptsAnalyticsUiState.Content)
                        ?: AttemptsAnalyticsUiState.Content()
                    content.copy(users = users)
                }
            }.onFailure { e ->
                _state.value = AttemptsAnalyticsUiState.Error(e.message ?: "Ошибка загрузки пользователей")
            }
        }
    }

    private fun loadAttempts(quizId: Int, employeeId: Int?) {
        viewModelScope.launch {
            _state.update { s ->
                val content = (s as? AttemptsAnalyticsUiState.Content)
                    ?: AttemptsAnalyticsUiState.Content()
                content.copy(isAttemptsListLoading = true)
            }

            runCatching {
                val attempts = getQuizAttemptsAnalyticsUseCase(quizId, employeeId)

                attempts.map {
                    it.toUiModel()
                }
            }.onSuccess { attempts ->
                _state.update { s ->
                    val content = (s as? AttemptsAnalyticsUiState.Content)
                        ?: AttemptsAnalyticsUiState.Content()
                    content.copy(
                        attempts = attempts,
                        isAttemptsListLoading = false
                    )
                }
            }.onFailure { e ->
                _state.value = AttemptsAnalyticsUiState.Error(e.message ?: "Ошибка загрузки попыток")
            }
        }
    }

    private fun updateSelectedEmployee(employeeId: Int?) {
        _state.update { s ->
            val content = (s as? AttemptsAnalyticsUiState.Content)
                ?: AttemptsAnalyticsUiState.Content()
            content.copy(selectedEmployeeId = employeeId)
        }
    }
}

sealed interface AttemptsAnalyticsCommand {
    data class Init(val quizId: Int) : AttemptsAnalyticsCommand
    data class EmployeeSelected(val employeeId: Int?) : AttemptsAnalyticsCommand
    data object FinishClicked : AttemptsAnalyticsCommand
}

sealed interface AttemptsAnalyticsEvent {
    data object Finish : AttemptsAnalyticsEvent
}

sealed interface AttemptsAnalyticsUiState {
    data object Initial : AttemptsAnalyticsUiState

    data class Content(
        val users: List<User> = listOf(),
        val attempts: List<AttemptAnalyticsUiModel> = listOf(),
        val selectedEmployeeId: Int? = null,
        val isAttemptsListLoading: Boolean = false
    ) : AttemptsAnalyticsUiState

    data class Error(val message: String) : AttemptsAnalyticsUiState
}