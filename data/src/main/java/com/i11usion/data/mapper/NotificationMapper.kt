package com.i11usion.data.mapper

import com.i11usion.core.domain.model.notification.Notification
import com.i11usion.core.domain.model.notification.NotificationType
import com.i11usion.core.network.model.notification.NotificationDto
import java.time.Instant

fun NotificationDto.toDomain(): Notification {

    val notificationType = NotificationType.fromApi(type)

    val payloadId: Int? = when (notificationType) {

        NotificationType.TASK_ASSIGNED -> {
            payload?.get("task_id")?.asInt
        }

        NotificationType.QUIZ_ASSIGNED -> {
            payload?.get("quiz_id")?.asInt
        }

        NotificationType.ATTEMPT_COMPLETED -> {
            payload?.get("attempt_id")?.asInt
        }

        NotificationType.COMMENT_ADDED -> {
            payload?.get("task_id")?.asInt
        }

        NotificationType.ATTACHMENT_ADDED -> {
            payload?.get("task_id")?.asInt
        }

        NotificationType.TASK_PROGRESS_UPDATED -> {
            payload?.get("task_id")?.asInt
        }
    }

    return Notification(
        id = id,
        userId = userId,
        type = notificationType,
        title = title,
        body = body,
        isRead = isRead,
        createdAt = Instant.parse(createdAt),
        payloadId = payloadId
    )
}