package com.i11usion.feature.main.presentation.quiz.testing.analytics.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.i11usion.core.domain.model.user.Role
import com.i11usion.core.domain.usecase.quiz.attempt.GetAttemptInfoByIdUseCase
import com.i11usion.core.domain.usecase.session.GetUserInfoUseCase
import com.i11usion.feature.main.presentation.quiz.testing.analytics.detail.mapper.toUiModel
import com.i11usion.feature.main.presentation.quiz.testing.analytics.detail.model.AttemptReadUiModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

class AttemptAnalyticsDetailViewModel @Inject constructor(

    private val getUserInfoUseCase: GetUserInfoUseCase,
    private val getAttemptInfoByIdUseCase: GetAttemptInfoByIdUseCase
) : ViewModel() {

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
        }
    }

    private fun loadAttempt(attemptId: Int) {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            _state.value = AttemptAnalyticsDetailUiState.Loading

            runCatching {
                val user = getUserInfoUseCase()
                requireNotNull(user) { "Не удалось получить пользователя" }
                val role: Role = user.role

                val attemptDomain = getAttemptInfoByIdUseCase(attemptId)

                val attemptUi: AttemptReadUiModel = attemptDomain.toUiModel()

                AttemptAnalyticsDetailUiState.Content(
                    user = role,
                    attempt = attemptUi
                )
            }.onSuccess { content ->
                _state.value = content
            }.onFailure { t ->
                _state.value = AttemptAnalyticsDetailUiState.Error(
                    message = t.message ?: "Unknown error"
                )
            }
        }
    }
}

sealed interface AttemptAnalyticsDetailCommand {
    data class Init(val attemptId: Int) : AttemptAnalyticsDetailCommand
    data class EmployeeSelected(val employeeId: Int?) : AttemptAnalyticsDetailCommand
    data object FinishClicked : AttemptAnalyticsDetailCommand
}

sealed interface AttemptAnalyticsDetailEvent {
    data object Finish : AttemptAnalyticsDetailEvent
}

sealed interface AttemptAnalyticsDetailUiState {
    data object Initial : AttemptAnalyticsDetailUiState

    data object Loading : AttemptAnalyticsDetailUiState

    data class Content(
        val user: Role,
        val attempt: AttemptReadUiModel
    ) : AttemptAnalyticsDetailUiState

    data class Error(val message: String) : AttemptAnalyticsDetailUiState
}