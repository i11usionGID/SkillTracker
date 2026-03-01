package com.i11usion.core.domain.usecase.notification

import com.i11usion.core.domain.model.network.AppResult
import com.i11usion.core.domain.model.notification.Notification
import com.i11usion.core.domain.repository.NotificationRepository
import javax.inject.Inject

class ReadNotificationUseCase @Inject constructor(
    private val repository: NotificationRepository
) {

    suspend operator fun invoke(notificationId: Int): AppResult<Notification> {
        return repository.readNotification(notificationId)
    }
}