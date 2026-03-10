package com.i11usion.core.domain.model.quiz.attempt

import java.time.Instant

data class Attempt(
    val id: Int,
    val quizId: Int,
    val assignmentId: Int?,
    val userId: Int,
    val userName: String?,
    val status: AttemptStatus,
    val scorePercent: Double?,
    val passed: Boolean?,
    val startedAt: Instant,
    val finishedAt: Instant?
)
