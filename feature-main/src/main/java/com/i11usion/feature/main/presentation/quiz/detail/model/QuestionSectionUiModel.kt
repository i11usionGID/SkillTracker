package com.i11usion.feature.main.presentation.quiz.detail.model

data class QuestionSectionUiModel(
    val id: Int,
    val quizId: Int,
    val title: String,
    val description: String?,
    val orderIndex: Int,
    val pickCount: Int?,
    val questions: List<SectionQuestionsUiModel>
)