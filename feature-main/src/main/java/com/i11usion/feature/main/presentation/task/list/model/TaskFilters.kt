package com.i11usion.feature.main.presentation.task.list.model

import com.i11usion.core.domain.model.task.TaskStatus

data class TaskFilters(
    val query: String = "",
    val status: TaskStatus? = null,
    val employeeId: Int? = null
)