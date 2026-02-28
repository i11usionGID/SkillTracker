package com.i11usion.core.domain.model.quiz.attempt

data class AttemptAnswer(
    val attemptQuestionId: Int,
    val questionId: Int,
    val isCorrect: Boolean?,
    val selectedOptionIds: List<Int>,
    val textAnswer: String?
)