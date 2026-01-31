package com.i11usion.feature.main.presentation.dashboard.model

import com.i11usion.core.domain.model.User

data class TaskUi(
    val id: Int,
    val title: String,
    val description: String?,
    val status: TaskUiStatus,
    val statusText: String,
    val progress: String,
    val deadline: String?,
    val createdAt: String,
    val updatedAt: String?,
    val employee: User
)
