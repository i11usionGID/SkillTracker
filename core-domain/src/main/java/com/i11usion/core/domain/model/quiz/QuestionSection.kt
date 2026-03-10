package com.i11usion.core.domain.model.quiz

data class QuestionSection(
    val id: Int,
    val quizId: Int,
    val title: String,
    val description: String?,
    val orderIndex: Int,
    val pickCount: Int?,
    val questions: List<Question>
)
