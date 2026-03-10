package com.i11usion.core.domain.model.quiz

data class QuestionOption(
    val id: Int,
    val text: String,
    val isCorrect: Boolean = false
)