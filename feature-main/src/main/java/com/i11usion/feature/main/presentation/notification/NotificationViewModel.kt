package com.i11usion.feature.main.presentation.notification

import AppError
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.i11usion.core.domain.model.network.AppResult
import com.i11usion.core.domain.model.notification.NotificationType
import com.i11usion.core.domain.usecase.notification.GetNotificationsUseCase
import com.i11usion.core.domain.usecase.notification.ReadAllNotificationsUseCase
import com.i11usion.core.domain.usecase.notification.ReadNotificationUseCase
import com.i11usion.core.navigation.HomeRoutes
import com.i11usion.core.ui.model.ErrorScreenModel
import com.i11usion.core.ui.model.UiError
import com.i11usion.core.ui.model.UiText
import com.i11usion.feature.main.manager.NotificationManager
import com.i11usion.feature.main.manager.SnackbarManager
import com.i11usion.feature.main.presentation.notification.mapper.toNotificationUiError
import com.i11usion.feature.main.presentation.notification.mapper.toUiModel
import com.i11usion.feature.main.presentation.notification.model.NotificationUiModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

class NotificationViewModel @Inject constructor(
    private val getNotificationsUseCase: GetNotificationsUseCase,
    private val readNotificationUseCase: ReadNotificationUseCase,
    private val readAllNotificationsUseCase: ReadAllNotificationsUseCase,
    private val notificationManager: NotificationManager,
    private val snackbarManager: SnackbarManager
) : ViewModel() {

    private val _state = MutableStateFlow<NotificationUiState>(NotificationUiState.Initial)
    val state = _state.asStateFlow()

    private val _events = MutableSharedFlow<NotificationEvent>(extraBufferCapacity = 1)
    val events = _events.asSharedFlow()

    private enum class LastAction { NONE, LOAD, READ_ALL }

    private var lastAction: LastAction = LastAction.NONE

    init {
        loadNotifications()
    }

    fun onErrorRefreshClick() {
        loadNotifications()
    }

    private fun loadNotifications() {
        lastAction = LastAction.LOAD

        viewModelScope.launch {
            _state.value = NotificationUiState.Loading

            when (val result = getNotificationsUseCase(limit = 30)) {
                is AppResult.Success -> {
                    _state.value = NotificationUiState.Content(
                        notificationList = result.data.map { it.toUiModel() }
                    )
                }

                is AppResult.Failure -> {
                    showUiError(result.error, contentFallback = null)
                }
            }
        }
    }

    fun readAllNotifications() {
        lastAction = LastAction.READ_ALL

        viewModelScope.launch {
            _state.update { s ->
                if (s is NotificationUiState.Content) s.copy(
                    isLoadingOverlay = true,
                    fullScreenError = null
                ) else s
            }

            when (val result = readAllNotificationsUseCase()) {
                is AppResult.Success -> {
                    notificationManager.readNotification()
                    loadNotifications()
                }

                is AppResult.Failure -> {
                    _state.update { s ->
                        if (s is NotificationUiState.Content) s.copy(isLoadingOverlay = false) else s
                    }
                    showUiError(
                        result.error,
                        contentFallback = _state.value as? NotificationUiState.Content
                    )
                }
            }
        }
    }

    fun onNotificationClick(notification: NotificationUiModel) {
        viewModelScope.launch {
            when (readNotificationUseCase(notification.id)) {
                is AppResult.Failure -> {
                    snackbarManager.show(UiText.Dynamic("Не удалось отметить уведомление прочитанным"))
                    return@launch
                }

                is AppResult.Success -> Unit
            }

            val route: String = when (notification.type) {
                NotificationType.TASK_ASSIGNED,
                NotificationType.COMMENT_ADDED,
                NotificationType.ATTACHMENT_ADDED,
                NotificationType.TASK_PROGRESS_UPDATED -> {
                    notification.payloadId?.let { HomeRoutes.taskDetail(it) } ?: HomeRoutes.TASKS
                }

                NotificationType.QUIZ_ASSIGNED -> HomeRoutes.TESTS

                NotificationType.ATTEMPT_COMPLETED -> {
                    notification.payloadId?.let { HomeRoutes.attemptAnalyticDetail(it) }
                        ?: HomeRoutes.TESTS
                }

                NotificationType.QUIZ_AI_READY -> {
                    notification.payloadId?.let { HomeRoutes.quizDetail(it) } ?: HomeRoutes.TESTS
                }

                NotificationType.QUIZ_AI_FAILED -> HomeRoutes.TESTS
            }

            notificationManager.readNotification()
            _events.tryEmit(NotificationEvent.NavigateTo(route))
        }
    }

    private fun showUiError(error: AppError, contentFallback: NotificationUiState.Content?) {
        when (val uiError = error.toNotificationUiError()) {
            is UiError.SnackBar -> snackbarManager.show(uiError.message)

            is UiError.FullScreen -> {
                val current = _state.value
                when {
                    current is NotificationUiState.Content ->
                        _state.value =
                            current.copy(fullScreenError = uiError.model, isLoadingOverlay = false)

                    contentFallback != null ->
                        _state.value = contentFallback.copy(
                            fullScreenError = uiError.model,
                            isLoadingOverlay = false
                        )

                    else ->
                        _state.value = NotificationUiState.Error(uiError.model)
                }
            }
        }
    }
}

sealed interface NotificationEvent {
    data class NavigateTo(val route: String) : NotificationEvent
}

sealed interface NotificationUiState {
    data object Initial : NotificationUiState
    data object Loading : NotificationUiState

    data class Content(
        val notificationList: List<NotificationUiModel>,
        val fullScreenError: ErrorScreenModel? = null,
        val isLoadingOverlay: Boolean = false
    ) : NotificationUiState

    data class Error(
        val model: ErrorScreenModel
    ) : NotificationUiState
}