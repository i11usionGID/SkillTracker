package com.i11usion.core.domain.model

import java.time.Instant

data class Quiz(
    val id: Int,
    val title: String,
    val description: String?,
    val passPercent: Int,
    val timeLimitSeconds: Int?,
    val shuffleQuestions: Boolean,
    val shuffleAnswers: Boolean,
    val allowReview: Boolean,
    val maxAttempts: Int?,
    val status: QuizStatus,
    val createdAt: Instant,
    val updatedAt: Instant
)
