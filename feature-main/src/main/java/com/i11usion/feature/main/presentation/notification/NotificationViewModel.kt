package com.i11usion.feature.main.presentation.notification

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.i11usion.core.domain.model.notification.NotificationType
import com.i11usion.core.domain.usecase.notification.GetNotificationsUseCase
import com.i11usion.core.domain.usecase.notification.ReadAllNotificationsUseCase
import com.i11usion.core.domain.usecase.notification.ReadNotificationUseCase
import com.i11usion.core.navigation.HomeRoutes
import com.i11usion.feature.main.presentation.notification.mapper.toUiModel
import com.i11usion.feature.main.presentation.notification.model.NotificationUiModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

class NotificationViewModel @Inject constructor(
    private val getNotificationsUseCase: GetNotificationsUseCase,
    private val readNotificationUseCase: ReadNotificationUseCase,
    private val readAllNotificationsUseCase: ReadAllNotificationsUseCase
) : ViewModel() {

    private val _state = MutableStateFlow<NotificationUiState>(NotificationUiState.Initial)
    val state = _state.asStateFlow()

    private val _events = MutableSharedFlow<NotificationEvent>()
    val events = _events.asSharedFlow()

    init {
        loadNotifications()
    }

    private fun loadNotifications() {
        viewModelScope.launch {
            _state.value = NotificationUiState.Loading

            try {
                val notifications = getNotificationsUseCase(limit = 30)

                _state.value = NotificationUiState.Content(
                    notificationList = notifications.map { it.toUiModel() }
                )
            } catch (e: Exception) {
                _state.value = NotificationUiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun readAllNotifications() {
        viewModelScope.launch {
            readAllNotificationsUseCase()
            loadNotifications()
        }
    }

    fun onNotificationClick(notification: NotificationUiModel) {
        viewModelScope.launch {

            readNotificationUseCase(notification.id)

            val route = when (notification.type) {

                NotificationType.TASK_ASSIGNED,
                NotificationType.COMMENT_ADDED,
                NotificationType.ATTACHMENT_ADDED,
                NotificationType.TASK_PROGRESS_UPDATED -> {
                    notification.payloadId?.let {
                        HomeRoutes.taskDetail(it)
                    } ?: {
                        HomeRoutes.TASKS
                    }
                }

                NotificationType.QUIZ_ASSIGNED -> {
                    HomeRoutes.TESTS
                }

                NotificationType.ATTEMPT_COMPLETED -> {
                    notification.payloadId?.let {
                        HomeRoutes.attemptAnalyticDetail(it)
                    } ?: HomeRoutes.TESTS
                }
            }

            _events.emit(NotificationEvent.NavigateTo(route as String))
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
        val notificationList: List<NotificationUiModel>
    ) : NotificationUiState

    data class Error(
        val message: String
    ) : NotificationUiState
}