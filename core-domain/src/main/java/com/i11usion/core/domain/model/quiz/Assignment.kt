package com.i11usion.core.domain.model.quiz

import java.time.Instant

data class Assignment(
    val id: Int,
    val quizId: Int,
    val assignedToUserId: Int?,
    val taskId: Int?,
    val required: Boolean,
    val dueAt: Instant?,
    val maxAttempts: Int?,
    val cooldownHours: Int,
    val createdAt: Instant,
    val attemptsTaken: Int,
    val remainingAttempts: Int?
)
