package com.i11usion.core.domain.repository

import com.i11usion.core.domain.model.network.AppResult
import com.i11usion.core.domain.model.notification.Notification

interface NotificationRepository {
    suspend fun getNotifications(
        unreadOnly: Boolean?,
        limit: Int?,
        offset: Int?
    ): AppResult<List<Notification>>

    suspend fun readNotification(notificationId: Int): AppResult<Notification>
    suspend fun readAllNotifications(): AppResult<Unit>
}