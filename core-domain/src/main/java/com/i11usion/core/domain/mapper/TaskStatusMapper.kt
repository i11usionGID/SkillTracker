package com.i11usion.core.domain.mapper

import com.i11usion.core.domain.model.TaskStatus

fun TaskStatus.taskStatusToString(): String? {
    return when(this) {
        TaskStatus.PENDING -> "pending"
        TaskStatus.IN_PROGRESS -> "in_progress"
        TaskStatus.DONE -> "done"
        TaskStatus.UNKNOWN -> null
    }
}