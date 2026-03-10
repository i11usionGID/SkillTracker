package com.i11usion.feature.main.presentation.quiz.list.model

import com.i11usion.core.domain.model.quiz.QuizStatus

data class QuizManagerUiModel(
    val id: Int,
    val title: String,
    val description: String?,
    val passPercent: String,
    val timeLimitSeconds: String?,
    val maxAttempts: String?,
    val status: QuizStatus,
    val statusText: String,
    val createdAt: String
)
