package com.i11usion.feature.main.presentation.quiz.testing.analytics.list.model

import com.i11usion.core.domain.model.quiz.attempt.AttemptStatus

data class AttemptAnalyticsUiModel(
    val id: Int,
    val userName: String?,
    val status: AttemptStatus,
    val scorePercent: Double?,
    val passed: Boolean?,
    val startedAt: String,
    val finishedAt: String?
)
