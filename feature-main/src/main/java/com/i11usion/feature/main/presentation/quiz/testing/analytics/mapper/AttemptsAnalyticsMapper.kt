package com.i11usion.feature.main.presentation.quiz.testing.analytics.mapper

import com.i11usion.core.domain.model.quiz.attempt.Attempt
import com.i11usion.core.ui.formatter.formatInstantToStringData
import com.i11usion.core.ui.formatter.formatInstantToStringDataNullable
import com.i11usion.feature.main.presentation.quiz.testing.analytics.model.AttemptAnalyticsUiModel

fun Attempt.toUiModel(): AttemptAnalyticsUiModel =
    AttemptAnalyticsUiModel(
        id = id,
        userName = userName,
        status = status,
        scorePercent = scorePercent,
        passed = passed,
        startedAt = formatInstantToStringData(startedAt),
        finishedAt = formatInstantToStringDataNullable(finishedAt)
    )