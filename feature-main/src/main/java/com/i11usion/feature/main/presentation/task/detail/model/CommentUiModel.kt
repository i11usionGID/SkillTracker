package com.i11usion.feature.main.presentation.task.detail.model

data class CommentUiModel(
    val id: Int,
    val taskId: Int,
    val ownerId: Int,
    val text: String,
    val createdAt: String,
    val authorName: String
)