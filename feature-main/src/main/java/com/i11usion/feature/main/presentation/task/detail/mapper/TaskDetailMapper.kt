package com.i11usion.feature.main.presentation.task.detail.mapper

import com.i11usion.core.domain.model.task.Attachment
import com.i11usion.core.domain.model.task.Comment
import com.i11usion.core.ui.formatter.formatInstantToStringData
import com.i11usion.feature.main.presentation.task.detail.model.AttachmentUiModel
import com.i11usion.feature.main.presentation.task.detail.model.CommentUiModel

fun Comment.domainToUi(): CommentUiModel {
    return CommentUiModel(
        id = id,
        taskId = taskId,
        ownerId = userId,
        text = text,
        createdAt = formatInstantToStringData(createdAt),
        authorName = author.name
    )
}

fun Attachment.domainToUi(): AttachmentUiModel {
    return AttachmentUiModel(
        id = id,
        taskId = taskId,
        uploadedById = uploadedById,
        storedPath = storedPath,
        originalFileName = originalFileName,
        contentType = contentType,
        createdAt = formatInstantToStringData(createdAt)
    )
}