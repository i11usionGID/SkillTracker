package com.i11usion.feature.main.presentation.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.i11usion.core.domain.model.User
import com.i11usion.core.domain.usecase.GetDashboardDataUseCase
import com.i11usion.core.domain.usecase.LoadProfileUseCase
import com.i11usion.feature.main.presentation.dashboard.mapper.dashboardDataToDashboardDataUi
import com.i11usion.feature.main.presentation.dashboard.model.DashboardDataUi
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

class DashboardViewModel @Inject constructor(
    private val loadProfileUseCase: LoadProfileUseCase,
    private val getDashboardDataUseCase: GetDashboardDataUseCase
) : ViewModel() {

    private val _state = MutableStateFlow<DashboardUiState>(DashboardUiState.Initial)
    val state = _state.asStateFlow()

    init {
        loadDashboardData()
    }

    private fun loadDashboardData() {
        _state.value = DashboardUiState.Loading

        viewModelScope.launch {
            try {
                val (user, dashboardData) = coroutineScope {
                    val userDeferred = async { loadProfileUseCase() }
                    val dataDeferred = async { getDashboardDataUseCase() }
                    userDeferred.await() to dataDeferred.await()
                }

                _state.value = DashboardUiState.Content(
                    user = user,
                    dashboardData = dashboardData.dashboardDataToDashboardDataUi()
                )
            } catch (t: Throwable) {
                _state.value = DashboardUiState.Error(
                    message = t.message ?: "Не удалось загрузить данные"
                )
            }
        }
    }

}

sealed interface DashboardUiState {
    data object Initial : DashboardUiState
    data object Loading : DashboardUiState

    data class Content(
        val user: User,
        val dashboardData: DashboardDataUi
    ) : DashboardUiState

    data class Error(
        val message: String
    ) : DashboardUiState
}