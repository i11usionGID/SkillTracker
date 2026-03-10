package com.i11usion.feature.main.presentation.quiz.testing.analytics.list

import AppError
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.i11usion.core.domain.model.network.AppResult
import com.i11usion.core.domain.model.user.User
import com.i11usion.core.domain.usecase.quiz.attempt.GetQuizAttemptsAnalyticsUseCase
import com.i11usion.core.domain.usecase.user.GetUsersListUseCase
import com.i11usion.core.ui.model.ErrorScreenModel
import com.i11usion.core.ui.model.UiError
import com.i11usion.core.ui.model.UiText
import com.i11usion.feature.main.manager.SnackbarManager
import com.i11usion.feature.main.mapper.toScreenUiError
import com.i11usion.feature.main.presentation.quiz.testing.analytics.list.mapper.toUiModel
import com.i11usion.feature.main.presentation.quiz.testing.analytics.list.model.AttemptAnalyticsUiModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

class AttemptsAnalyticsViewModel @Inject constructor(
    private val getUsersListUseCase: GetUsersListUseCase,
    private val getQuizAttemptsAnalyticsUseCase: GetQuizAttemptsAnalyticsUseCase,
    private val snackbarManager: SnackbarManager
) : ViewModel() {

    private companion object {
        const val RETRY_DELAY_MS = 10_000L
    }

    private var currentQuizId: Int? = null
    private var loadEmployeesJob: Job? = null
    private var loadAttemptsJob: Job? = null

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

            AttemptsAnalyticsCommand.Refresh -> onErrorRefreshClick()
        }
    }

    private fun onErrorRefreshClick() {
        when (val state = _state.value) {
            is AttemptsAnalyticsUiState.Content -> {
                val quizId = currentQuizId ?: return
                _state.value = state.copy(
                    fullScreenError = null,
                    isAttemptsListLoading = true
                )
                loadEmployees()
                loadAttempts(quizId, state.selectedEmployeeId)
            }

            else -> {
                val quizId = currentQuizId ?: return
                init(quizId)
            }
        }
    }

    private fun init(quizId: Int) {
        currentQuizId = quizId
        loadEmployeesJob?.cancel()
        loadAttemptsJob?.cancel()

        _state.value = AttemptsAnalyticsUiState.Content(
            users = emptyList(),
            attempts = emptyList(),
            selectedEmployeeId = null,
            isAttemptsListLoading = true,
            fullScreenError = null
        )

        loadEmployees()
        loadAttempts(quizId, employeeId = null)
    }

    private fun loadEmployees() {
        loadEmployeesJob?.cancel()
        loadEmployeesJob = viewModelScope.launch {
            when (
                val result = retryRequest {
                    getUsersListUseCase(role = "employee")
                }
            ) {
                is AppResult.Success -> {
                    _state.update { s ->
                        val content = (s as? AttemptsAnalyticsUiState.Content)
                            ?: AttemptsAnalyticsUiState.Content()
                        content.copy(
                            users = result.data
                        )
                    }
                }

                is AppResult.Failure -> {
                    showUiError(result.error)
                }
            }
        }
    }

    private fun loadAttempts(quizId: Int, employeeId: Int?) {
        loadAttemptsJob?.cancel()
        loadAttemptsJob = viewModelScope.launch {
            _state.update { s ->
                val content = (s as? AttemptsAnalyticsUiState.Content)
                    ?: AttemptsAnalyticsUiState.Content()
                content.copy(
                    isAttemptsListLoading = true
                )
            }

            when (
                val result = retryRequest {
                    getQuizAttemptsAnalyticsUseCase(quizId, employeeId)
                }
            ) {
                is AppResult.Success -> {
                    _state.update { s ->
                        val content = (s as? AttemptsAnalyticsUiState.Content)
                            ?: AttemptsAnalyticsUiState.Content()
                        content.copy(
                            attempts = result.data.map { it.toUiModel() },
                            isAttemptsListLoading = false
                        )
                    }
                }

                is AppResult.Failure -> {
                    _state.update { s ->
                        val content = (s as? AttemptsAnalyticsUiState.Content)
                            ?: AttemptsAnalyticsUiState.Content()
                        content.copy(
                            isAttemptsListLoading = false
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

    private fun updateSelectedEmployee(employeeId: Int?) {
        _state.update { s ->
            val content = (s as? AttemptsAnalyticsUiState.Content)
                ?: AttemptsAnalyticsUiState.Content()
            content.copy(selectedEmployeeId = employeeId)
        }
    }

    private fun showUiError(error: AppError) {
        when (val uiError = error.toScreenUiError()) {
            is UiError.SnackBar -> snackbarManager.show(uiError.message)

            is UiError.FullScreen -> {
                val current = _state.value
                if (current is AttemptsAnalyticsUiState.Content) {
                    _state.value = current.copy(fullScreenError = uiError.model)
                } else {
                    _state.value = AttemptsAnalyticsUiState.Error(uiError.model)
                }
            }
        }
    }
}

sealed interface AttemptsAnalyticsCommand {
    data class Init(val quizId: Int) : AttemptsAnalyticsCommand
    data class EmployeeSelected(val employeeId: Int?) : AttemptsAnalyticsCommand
    data object FinishClicked : AttemptsAnalyticsCommand
    data object Refresh : AttemptsAnalyticsCommand
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
        val isAttemptsListLoading: Boolean = false,
        val fullScreenError: ErrorScreenModel? = null
    ) : AttemptsAnalyticsUiState

    data class Error(val model: ErrorScreenModel) : AttemptsAnalyticsUiState
}