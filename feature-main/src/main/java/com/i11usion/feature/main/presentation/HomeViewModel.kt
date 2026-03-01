package com.i11usion.feature.main.presentation

import AppError
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.i11usion.core.domain.model.network.AppResult
import com.i11usion.core.domain.model.user.Role
import com.i11usion.core.domain.usecase.auth.LogoutUseCase
import com.i11usion.core.domain.usecase.notification.GetNotificationsUseCase
import com.i11usion.core.domain.usecase.session.GetUserInfoUseCase
import com.i11usion.core.ui.model.ErrorScreenModel
import com.i11usion.core.ui.model.UiError
import com.i11usion.core.ui.model.UiText
import com.i11usion.feature.main.manager.NotificationManager
import com.i11usion.feature.main.manager.SnackbarManager
import com.i11usion.feature.main.mapper.toHomeUiError
import com.i11usion.feature.main.presentation.drawer.DrawerConfig
import com.i11usion.feature.main.presentation.drawer.DrawerItem
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.coroutines.coroutineContext

class HomeViewModel @Inject constructor(
    private val getUserInfoUseCase: GetUserInfoUseCase,
    private val logoutUseCase: LogoutUseCase,
    private val getNotificationsUseCase: GetNotificationsUseCase,
    private val notificationManager: NotificationManager,
    val snackbarManager: SnackbarManager
) : ViewModel() {

    private val _state = MutableStateFlow<HomeState>(HomeState.Initial)
    val state = _state.asStateFlow()

    private val _events = MutableSharedFlow<HomeEvent>(extraBufferCapacity = 1)
    val events = _events.asSharedFlow()

    private var notificationsJob: Job? = null
    private var retryAction: (() -> Unit)? = null
    private var notificationsHadError: Boolean = false

    init {
        _state.value = HomeState.Loading
        loadUser()

        viewModelScope.launch {
            notificationManager.events.collectLatest {
                val count = fetchUnreadCountOnce()
                if (count != null) updateContent { it.copy(notificationCount = count) }
            }
        }
    }

    fun onLogoutClick() {
        retryAction = ::onLogoutClick

        setLoadingOverlay(true)
        stopNotificationsPolling()

        viewModelScope.launch {
            when (val result = logoutUseCase()) {
                is AppResult.Failure -> {
                    showUiError(result.error)
                    setLoadingOverlay(false)
                }

                is AppResult.Success -> _events.tryEmit(HomeEvent.Logout)
            }
        }
    }

    fun onErrorRefreshClick() {
        clearFullScreenErrorIfNeeded()
        (retryAction ?: ::loadUser).invoke()
    }

    private fun loadUser() {
        retryAction = ::loadUser
        stopNotificationsPolling()

        val user = getUserInfoUseCase()
        if (user == null) {
            _state.value = HomeState.Error(
                model = ErrorScreenModel(
                    iconResId = com.i11usion.core.ui.R.drawable.ic_fail,
                    cdResId = com.i11usion.core.ui.R.string.cd_unknown_error,
                    text = UiText.Dynamic("Не удалось загрузить данные пользователя")
                )
            )
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
        stopNotificationsPolling()

        notificationsJob = viewModelScope.launch {
            unreadCountPollingFlow(pollMs)
                .distinctUntilChanged()
                .collect { count ->
                    updateContent { it.copy(notificationCount = count) }
                }
        }
    }

    private fun stopNotificationsPolling() {
        notificationsJob?.cancel()
        notificationsJob = null
    }

    private fun unreadCountPollingFlow(pollMs: Long) = flow {
        while (coroutineContext.isActive) {
            val countOrNull = fetchUnreadCountOnce()
            if (countOrNull != null) emit(countOrNull)
            delay(pollMs)
        }
    }

    private suspend fun fetchUnreadCountOnce(): Int? {
        return when (val result = getNotificationsUseCase(
            unreadOnly = true,
            limit = 10,
            offset = 0
        )) {
            is AppResult.Failure -> {
                if (!notificationsHadError) {
                    notificationsHadError = true
                    snackbarManager.show(UiText.Dynamic("Не удалось обновить уведомления"))
                }
                null
            }

            is AppResult.Success -> {
                notificationsHadError = false
                result.data.size
            }
        }
    }

    private fun showUiError(error: AppError) {
        when (val uiError = error.toHomeUiError()) {
            is UiError.SnackBar -> snackbarManager.show(uiError.message)

            is UiError.FullScreen -> {
                val s = _state.value
                if (s is HomeState.Content) {
                    _state.value = s.copy(fullScreenError = uiError.model, isLoadingOverlay = false)
                } else {
                    _state.value = HomeState.Error(uiError.model)
                }
            }
        }
    }

    private fun updateContent(block: (HomeState.Content) -> HomeState.Content) {
        val s = _state.value
        if (s is HomeState.Content) _state.value = block(s)
    }

    private fun setLoadingOverlay(isLoading: Boolean) {
        when (val s = _state.value) {
            is HomeState.Content -> _state.value =
                s.copy(isLoadingOverlay = isLoading, fullScreenError = null)

            else -> if (isLoading) _state.value = HomeState.Loading
        }
    }

    private fun clearFullScreenErrorIfNeeded() {
        when (val s = _state.value) {
            is HomeState.Content -> _state.value = s.copy(fullScreenError = null)
            is HomeState.Error -> Unit
            else -> Unit
        }
    }

    override fun onCleared() {
        stopNotificationsPolling()
        super.onCleared()
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
        val notificationCount: Int = 0,
        val fullScreenError: ErrorScreenModel? = null,
        val isLoadingOverlay: Boolean = false
    ) : HomeState

    data class Error(
        val model: ErrorScreenModel
    ) : HomeState
}

sealed interface HomeEvent {
    data object Logout : HomeEvent
}

