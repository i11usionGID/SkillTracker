package com.i11usion.feature.main.presentation.quiz.detail.model

data class AssignmentDetailUiModel(
    val id: Int,
    val required: Boolean,
    val allowedUserUsername: String,
    val allowedUserName: String,
    val taskTitle: String?,
    val dueAt: String?,
    val maxAttempts: Int?,
    val cooldownHours: Int,
    val attemptsTaken: Int,
    val remainingAttempts: Int?
)
