package com.i11usion.feature.main.presentation.task.detail.model

data class AttachmentUiModel(
    val id: Int,
    val taskId: Int,
    val uploadedById: Int,
    val storedPath: String,
    val originalFileName: String,
    val contentType: String?,
    val createdAt: String
)