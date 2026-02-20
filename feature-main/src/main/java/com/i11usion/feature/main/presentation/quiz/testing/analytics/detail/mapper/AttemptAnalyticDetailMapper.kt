package com.i11usion.feature.main.presentation.quiz.testing.analytics.detail.mapper

import com.i11usion.core.domain.model.quiz.attempt.AttemptRead
import com.i11usion.core.ui.formatter.formatInstantToStringData
import com.i11usion.core.ui.formatter.formatInstantToStringDataNullable
import com.i11usion.feature.main.presentation.quiz.testing.analytics.detail.model.AttemptReadUiModel

fun AttemptRead.toUiModel(): AttemptReadUiModel =
    AttemptReadUiModel(
        id = id,
        status = status,
        scorePercent = scorePercent,
        passed = passed,
        startedAt = formatInstantToStringData(startedAt),
        finishedAt = finishedAt?.let {
            formatInstantToStringDataNullable(finishedAt)
        },
        answers = answers
    )