package com.i11usion.core.domain.usecase.notification

import com.i11usion.core.domain.model.network.AppResult
import com.i11usion.core.domain.model.notification.Notification
import com.i11usion.core.domain.repository.NotificationRepository
import javax.inject.Inject

class GetNotificationsUseCase @Inject constructor(
    private val repository: NotificationRepository
) {

    suspend operator fun invoke(
        unreadOnly: Boolean? = null,
        limit: Int? = null,
        offset: Int? = null
    ): AppResult<List<Notification>> {
        return repository.getNotifications(
            unreadOnly = unreadOnly,
            limit = limit,
            offset = offset
        )
    }
}