package com.i11usion.core.domain.model.quiz

data class QuizCreate(
    val title: String,
    val description: String?,
    val passPercent: Int,
    val timeLimitSeconds: Int?,
    val shuffleQuestions: Boolean,
    val shuffleAnswers: Boolean,
    val allowReview: Boolean,
    val maxAttempts: Int?,
    val publish: Boolean
)