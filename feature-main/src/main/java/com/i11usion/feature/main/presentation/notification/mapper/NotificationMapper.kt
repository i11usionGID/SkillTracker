package com.i11usion.feature.main.presentation.notification.mapper

import com.i11usion.core.domain.model.notification.Notification
import com.i11usion.feature.main.presentation.notification.model.NotificationUiModel
import java.time.Duration
import java.time.Instant
import kotlin.math.max

fun Notification.toUiModel(): NotificationUiModel =
    NotificationUiModel(
        id = id,
        type = type,
        title = title,
        body = body,
        isRead = isRead,
        createdAt = createdAt.toRelativeTimeString(),
        payloadId = payloadId
    )

private fun Instant.toRelativeTimeString(now: Instant = Instant.now()): String {
    val duration = Duration.between(this, now)

    if (duration.isNegative) return "меньше минуты назад"

    val minutes = duration.toMinutes()
    val hours = duration.toHours()
    val days = duration.toDays()
    val months = days / 30
    val years = days / 365

    return when {
        minutes < 1 -> "меньше минуты назад"
        minutes < 60 -> "$minutes мин. назад"
        hours < 24 -> "$hours ч. назад"
        days < 30 -> "$days дн. назад"
        days < 365 -> "$months мес. назад"
        else -> "${max(years, 1)} г. назад"
    }
}