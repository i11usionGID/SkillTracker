package com.i11usion.core.domain.model.quiz

data class Question(
    val id: Int,
    val text: String,
    val type: QuestionType,
    val explanation: String?,
    val options: List<QuestionOption>,
    val textAnswers: List<TextAnswer>
)
