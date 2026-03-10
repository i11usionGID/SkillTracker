package com.i11usion.feature.main.presentation.quiz.list.model

import com.i11usion.core.domain.model.quiz.QuizStatus

data class QuizFilters(
    val query: String = "",
    val status: QuizStatus? = null,
)