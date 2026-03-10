package com.i11usion.core.domain.model.notification

import java.time.Instant

data class Notification(
    val id: Int,
    val userId: Int,
    val type: NotificationType,
    val title: String,
    val body: String?,
    val isRead: Boolean,
    val createdAt: Instant,
    val payloadId: Int?
)
