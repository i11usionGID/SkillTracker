package com.i11usion.core.domain.model.quiz.attempt

data class AttemptSubmitAnswer(
    val attemptQuestionId: Int,
    val selectedOptionIds: List<Int> = emptyList(),
    val textAnswer: String? = null
)