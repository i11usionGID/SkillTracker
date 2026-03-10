package com.i11usion.data.remote.repository

import com.i11usion.core.domain.model.network.AppResult
import com.i11usion.core.domain.model.notification.Notification
import com.i11usion.core.domain.repository.NotificationRepository
import com.i11usion.core.network.notification.NotificationService
import com.i11usion.data.mapper.toDomain
import com.i11usion.data.utils.SafeApiCaller
import javax.inject.Inject

class NotificationRepositoryImpl @Inject constructor(
    private val notificationService: NotificationService,
    private val safeApiCaller: SafeApiCaller
): NotificationRepository {

    override suspend fun getNotifications(
        unreadOnly: Boolean?,
        limit: Int?,
        offset: Int?
    ): AppResult<List<Notification>> {
        return safeApiCaller.call {
            notificationService.getNotifications(
                unreadOnly = unreadOnly,
                limit = limit,
                offset = offset
            ).map {
                it.toDomain()
            }
        }
    }

    override suspend fun readNotification(notificationId: Int): AppResult<Notification> {
        return safeApiCaller.call {
            notificationService.readNotification(notificationId).toDomain()
        }
    }

    override suspend fun readAllNotifications(): AppResult<Unit> {
        return safeApiCaller.call {
            notificationService.readAllNotification()
        }
    }
}