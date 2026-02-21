package com.i11usion.feature.main.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.i11usion.core.domain.model.user.Role
import com.i11usion.core.domain.usecase.session.GetUserInfoUseCase
import com.i11usion.core.domain.usecase.auth.LogoutUseCase
import com.i11usion.core.domain.usecase.notification.GetNotificationsUseCase
import com.i11usion.feature.main.presentation.drawer.DrawerConfig
import com.i11usion.feature.main.presentation.drawer.DrawerItem
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import javax.inject.Inject

class HomeViewModel @Inject constructor(
    private val getUserInfoUseCase: GetUserInfoUseCase,
    private val logoutUseCase: LogoutUseCase,
    private val getNotificationsUseCase: GetNotificationsUseCase
) : ViewModel() {

    private val _state = MutableStateFlow<HomeState>(HomeState.Initial)
    val state = _state.asStateFlow()

    private val _events = MutableSharedFlow<HomeEvent>()
    val events = _events.asSharedFlow()

    private var notificationsJob: Job? = null

    init {
        _state.value = HomeState.Loading
        loadUser()
    }

    fun onLogoutClick() {
        _state.value = HomeState.Loading

        notificationsJob?.cancel()
        notificationsJob = null

        viewModelScope.launch {
            runCatching { logoutUseCase() }
                .onSuccess { _events.emit(HomeEvent.Logout) }
                .onFailure { _state.value = HomeState.Error(message = "ERROR") }
        }
    }

    private fun loadUser() {
        val user = getUserInfoUseCase()
        if (user == null) {
            _state.value = HomeState.Error(message = "ERROR")
            return
        }

        val filtered = DrawerConfig.items.filter { user.role in it.allowedRoles }
        _state.value = HomeState.Content(
            userName = user.name,
            role = user.role,
            drawerItems = filtered,
            notificationCount = 0
        )

        startNotificationsPolling(pollMs = 30_000L)
    }

    private fun startNotificationsPolling(pollMs: Long) {
        notificationsJob?.cancel()

        notificationsJob = viewModelScope.launch {
            observeUnreadCount(pollMs)
                .distinctUntilChanged()
                .collect { count ->
                    val current = _state.value
                    if (current is HomeState.Content) {
                        _state.value = current.copy(notificationCount = count)
                    }
                }
        }
    }

    private fun observeUnreadCount(pollMs: Long) = flow {
        while (true) {
            val count = runCatching {
                val list = getNotificationsUseCase(
                    unreadOnly = true,
                    limit = 10,
                    offset = 0
                )
                list.size
            }.getOrDefault(0)

            emit(count)
            delay(pollMs)
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
        val selectedScreenRoute: String? = null,
        val notificationCount: Int = 0
    ) : HomeState

    data class Error(
        val message: String
    ) : HomeState
}

sealed interface HomeEvent {
    object Logout : HomeEvent
}

