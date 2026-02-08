package com.i11usion.core.domain.model

import java.time.Instant

data class QuizAssignment(
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
