package com.i11usion.feature.main.presentation.quiz.testing.analytics.detail.model

import com.i11usion.core.domain.model.quiz.attempt.AttemptAnswer
import com.i11usion.core.domain.model.quiz.attempt.AttemptStatus

data class AttemptReadUiModel(
    val id: Int,
    val status: AttemptStatus,
    val scorePercent: Double?,
    val passed: Boolean?,
    val startedAt: String,
    val finishedAt: String?,
    val answers: List<AttemptAnswer>
)
