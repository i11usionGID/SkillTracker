package com.i11usion.core.domain.model.quiz.attempt

import java.time.Instant

data class AttemptStart(
    val attemptId: Int,
    val quizId: Int,
    val status: AttemptStatus,
    val startedAt: Instant,
    val timeLimitSeconds: Int?,
    val questions: List<AttemptQuestion>
)
