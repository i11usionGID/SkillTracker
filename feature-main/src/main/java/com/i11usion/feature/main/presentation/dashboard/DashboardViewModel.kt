package com.i11usion.feature.main.presentation.dashboard

import AppError
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.i11usion.core.domain.model.network.AppResult
import com.i11usion.core.domain.model.user.User
import com.i11usion.core.domain.usecase.session.GetUserInfoUseCase
import com.i11usion.core.domain.usecase.task.GetDashboardDataUseCase
import com.i11usion.core.ui.model.ErrorScreenModel
import com.i11usion.core.ui.model.UiError
import com.i11usion.feature.main.manager.SnackbarManager
import com.i11usion.feature.main.mapper.toScreenUiError
import com.i11usion.feature.main.presentation.dashboard.mapper.dashboardDataToDashboardDataUi
import com.i11usion.feature.main.presentation.dashboard.model.DashboardDataUi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

class DashboardViewModel @Inject constructor(
    private val getUserInfoUseCase: GetUserInfoUseCase,
    private val getDashboardDataUseCase: GetDashboardDataUseCase,
    private val snackbarManager: SnackbarManager
) : ViewModel() {

    private val _state = MutableStateFlow<DashboardUiState>(DashboardUiState.Initial)
    val state = _state.asStateFlow()

    init {
        loadDashboardData()
    }

    fun processCommand(command: DashboardCommand) {
        when (command) {
            DashboardCommand.Refresh -> onErrorRefreshClick()
        }
    }

    private fun onErrorRefreshClick() {
        when (_state.value) {
            is DashboardUiState.Content -> _state.value =
                (_state.value as DashboardUiState.Content).copy(fullScreenError = null)

            else -> loadDashboardData()
        }
    }

    private fun loadDashboardData() {
        _state.value = DashboardUiState.Loading

        viewModelScope.launch {
            val user = getUserInfoUseCase()
            when (val result = getDashboardDataUseCase()) {
                is AppResult.Success -> {
                    user?.let {
                        _state.value = DashboardUiState.Content(
                            user = it,
                            dashboardData = result.data.dashboardDataToDashboardDataUi()
                        )
                    }
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
                if (current is DashboardUiState.Content) {
                    _state.value = current.copy(fullScreenError = uiError.model)
                } else {
                    _state.value = DashboardUiState.Error(uiError.model)
                }
            }
        }
    }
}

sealed interface DashboardCommand {
    data object Refresh : DashboardCommand
}

sealed interface DashboardUiState {
    data object Initial : DashboardUiState
    data object Loading : DashboardUiState

    data class Content(
        val user: User,
        val dashboardData: DashboardDataUi,
        val fullScreenError: ErrorScreenModel? = null
    ) : DashboardUiState

    data class Error(
        val model: ErrorScreenModel
    ) : DashboardUiState
}