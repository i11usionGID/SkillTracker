package com.i11usion.feature.main.presentation.quiz.model

import com.i11usion.core.domain.model.quiz.QuizStatus

data class QuizFilters(
    val query: String = "",
    val status: QuizStatus? = null,
)