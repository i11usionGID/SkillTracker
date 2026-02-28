package com.i11usion.core.domain.model.quiz.attempt

import java.time.Instant

data class AttemptRead(
    val id: Int,
    val quizId: Int,
    val userId: Int,
    val status: AttemptStatus,
    val scorePercent: Double?,
    val passed: Boolean?,
    val startedAt: Instant,
    val finishedAt: Instant?,
    val answers: List<AttemptAnswer>
)
