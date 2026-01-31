package com.i11usion.feature.main.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.i11usion.core.domain.model.Role
import com.i11usion.core.domain.usecase.GetUserInfoUseCase
import com.i11usion.core.domain.usecase.LogoutUseCase
import com.i11usion.feature.main.presentation.drawer.DrawerConfig
import com.i11usion.feature.main.presentation.drawer.DrawerItem
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

class HomeViewModel @Inject constructor(
    private val getUserInfoUseCase: GetUserInfoUseCase,
    private val logoutUseCase: LogoutUseCase
) : ViewModel() {

    private val _state = MutableStateFlow<HomeState>(HomeState.Initial)
    val state = _state.asStateFlow()

    private val _events = MutableSharedFlow<HomeEvent>()
    val events = _events.asSharedFlow()

    init {
        _state.value = HomeState.Loading
        loadUser()
    }

    fun onLogoutClick() {
        _state.value = HomeState.Loading
        viewModelScope.launch {
            runCatching { logoutUseCase() }
                .onSuccess {
                    _events.emit(HomeEvent.Logout)
                }
                .onFailure { e ->
                    _state.value = HomeState.Error(message = "ERROR")
                }
        }
    }

    private fun loadUser() {
        getUserInfoUseCase()?.let { user ->
            val filtered = DrawerConfig.items.filter { user.role in it.allowedRoles }
            _state.value = HomeState.Content(
                userName = user.name,
                role = user.role,
                drawerItems = filtered
            )
        } ?: {
            _state.value = HomeState.Error(message = "ERROR")
        }
    }
}


sealed interface HomeState {

    data object Initial : HomeState

    data object Loading : HomeState

    data class Content(
        val userName: String = "",
        val role: Role = Role.EMPLOYEE,
        val drawerItems: List<DrawerItem> = emptyList(),
        val selectedScreenRoute: String? = null
    ) : HomeState

    data class Error(
        val message: String
    ) : HomeState
}

sealed interface HomeEvent {
    object Logout : HomeEvent
}

