package com.i11usion.feature.main.presentation.notification.model

import com.i11usion.core.domain.model.notification.NotificationType

data class NotificationUiModel(
    val id: Int,
    val type: NotificationType,
    val title: String,
    val body: String?,
    val isRead: Boolean,
    val createdAt: String,
    val payloadId: Int?
)