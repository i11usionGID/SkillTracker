package com.i11usion.feature.main.presentation.task.detail.model

data class CommentBlock(
    val query: String,
    val isLoading: Boolean,
    val commentList: List<CommentUiModel>,
    val editableComment: EditableComment?
)