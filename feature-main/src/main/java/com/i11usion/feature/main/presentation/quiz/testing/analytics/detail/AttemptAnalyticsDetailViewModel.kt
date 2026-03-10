package com.i11usion.feature.main.presentation.quiz.testing.analytics.detail


import AppError
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.i11usion.core.domain.model.network.AppResult
import com.i11usion.core.domain.model.user.Role
import com.i11usion.core.domain.usecase.quiz.attempt.GetAttemptInfoByIdUseCase
import com.i11usion.core.domain.usecase.session.GetUserInfoUseCase
import com.i11usion.core.ui.model.ErrorScreenModel
import com.i11usion.core.ui.model.UiError
import com.i11usion.core.ui.model.UiText
import com.i11usion.feature.main.manager.SnackbarManager
import com.i11usion.feature.main.mapper.toScreenUiError
import com.i11usion.feature.main.presentation.quiz.testing.analytics.detail.AttemptAnalyticsDetailUiState.Content
import com.i11usion.feature.main.presentation.quiz.testing.analytics.detail.AttemptAnalyticsDetailUiState.Loading
import com.i11usion.feature.main.presentation.quiz.testing.analytics.detail.mapper.toUiModel
import com.i11usion.feature.main.presentation.quiz.testing.analytics.detail.model.AttemptReadUiModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

class AttemptAnalyticsDetailViewModel @Inject constructor(
    private val getUserInfoUseCase: GetUserInfoUseCase,
    private val getAttemptInfoByIdUseCase: GetAttemptInfoByIdUseCase,
    private val snackbarManager: SnackbarManager
) : ViewModel() {

    private companion object {
        const val RETRY_DELAY_MS = 10_000L
    }

    private var currentAttemptId: Int? = null
    private var loadJob: Job? = null

    private val _state =
        MutableStateFlow<AttemptAnalyticsDetailUiState>(AttemptAnalyticsDetailUiState.Initial)
    val state = _state.asStateFlow()

    private val _events = MutableSharedFlow<AttemptAnalyticsDetailEvent>(extraBufferCapacity = 1)
    val events = _events.asSharedFlow()

    fun processCommand(command: AttemptAnalyticsDetailCommand) {
        when (command) {
            is AttemptAnalyticsDetailCommand.Init -> {
                currentAttemptId = command.attemptId
                loadAttempt(attemptId = command.attemptId)
            }

            is AttemptAnalyticsDetailCommand.EmployeeSelected -> {
                val attemptId = currentAttemptId ?: return
                loadAttempt(attemptId = attemptId)
            }

            AttemptAnalyticsDetailCommand.FinishClicked -> {
                _events.tryEmit(AttemptAnalyticsDetailEvent.Finish)
            }

            AttemptAnalyticsDetailCommand.Refresh -> onErrorRefreshClick()
        }
    }

    private fun onErrorRefreshClick() {
        val attemptId = currentAttemptId ?: return

        when (val current = _state.value) {
            is Content -> {
                _state.value = current.copy(fullScreenError = null)
                loadAttempt(attemptId)
            }

            else -> {
                loadAttempt(attemptId)
            }
        }
    }

    private fun loadAttempt(attemptId: Int) {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            _state.value = Loading

            val user = getUserInfoUseCase()
            requireNotNull(user) { "Не удалось получить пользователя" }
            val role: Role = user.role

            when (val result = retryRequest { getAttemptInfoByIdUseCase(attemptId) }) {
                is AppResult.Success -> {
                    val attemptUi: AttemptReadUiModel = result.data.toUiModel()

                    _state.value = Content(
                        user = role,
                        attempt = attemptUi,
                        fullScreenError = null
                    )
                }

                is AppResult.Failure -> {
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

    private fun showUiError(error: AppError) {
        when (val uiError = error.toScreenUiError()) {
            is UiError.SnackBar -> snackbarManager.show(uiError.message)

            is UiError.FullScreen -> {
                val current = _state.value
                if (current is Content) {
                    _state.value = current.copy(fullScreenError = uiError.model)
                } else {
                    _state.value = AttemptAnalyticsDetailUiState.Error(uiError.model)
                }
            }
        }
    }
}

sealed interface AttemptAnalyticsDetailCommand {
    data class Init(val attemptId: Int) : AttemptAnalyticsDetailCommand
    data class EmployeeSelected(val employeeId: Int?) : AttemptAnalyticsDetailCommand
    data object FinishClicked : AttemptAnalyticsDetailCommand
    data object Refresh : AttemptAnalyticsDetailCommand
}

sealed interface AttemptAnalyticsDetailEvent {
    data object Finish : AttemptAnalyticsDetailEvent
}

sealed interface AttemptAnalyticsDetailUiState {
    data object Initial : AttemptAnalyticsDetailUiState

    data object Loading : AttemptAnalyticsDetailUiState

    data class Content(
        val user: Role,
        val attempt: AttemptReadUiModel,
        val fullScreenError: ErrorScreenModel? = null
    ) : AttemptAnalyticsDetailUiState

    data class Error(val model: ErrorScreenModel) : AttemptAnalyticsDetailUiState
}