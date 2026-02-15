package com.i11usion.feature.main.presentation.quiz.list.model

data class QuizEmployeeUiModel(
    val id: Int,
    val title: String,
    val description: String?,
    val required: Boolean,
    val passPercent: String,
    val timeLimitSeconds: String?,
    val attempts: String?,
    val isFirstAttempt: Boolean?,
    val isNoAttempts: Boolean,
    val deadline: String?
)
