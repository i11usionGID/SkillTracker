package com.i11usion.feature.main.presentation.task.model

import com.i11usion.core.domain.model.TaskStatus

data class TaskFilters(
    val query: String = "",
    val status: TaskStatus? = null,
    val employeeId: Int? = null
)

