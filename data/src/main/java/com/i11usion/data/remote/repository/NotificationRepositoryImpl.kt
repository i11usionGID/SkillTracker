package com.i11usion.data.remote.repository

import com.i11usion.core.domain.model.notification.Notification
import com.i11usion.core.domain.repository.NotificationRepository
import com.i11usion.core.network.notification.NotificationService
import com.i11usion.data.mapper.toDomain
import javax.inject.Inject

class NotificationRepositoryImpl @Inject constructor(
    private val notificationService: NotificationService
): NotificationRepository {

    override suspend fun getNotifications(
        unreadOnly: Boolean?,
        limit: Int?,
        offset: Int?
    ): List<Notification> {
        return notificationService.getNotifications(
            unreadOnly = unreadOnly,
            limit = limit,
            offset = offset
        ).map {
            it.toDomain()
        }
    }

    override suspend fun readNotification(notificationId: Int): Notification {
        return notificationService.readNotification(notificationId).toDomain()
    }

    override suspend fun readAllNotifications() {
        notificationService.readAllNotification()
    }
}