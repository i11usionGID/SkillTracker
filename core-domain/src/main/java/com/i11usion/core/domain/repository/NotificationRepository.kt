package com.i11usion.core.domain.repository

import com.i11usion.core.domain.model.notification.Notification

interface NotificationRepository {
    suspend fun getNotifications(
        unreadOnly: Boolean?,
        limit: Int?,
        offset: Int?
    ): List<Notification>

    suspend fun readNotification(notificationId: Int): Notification
    suspend fun readAllNotifications()
}