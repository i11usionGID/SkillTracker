package com.i11usion.core.domain.mapper

import com.i11usion.core.domain.model.dashboard.DashboardStats
import com.i11usion.core.domain.model.task.Task
import com.i11usion.core.domain.model.task.TaskStatus

fun List<Task>.toStats(): DashboardStats {
    val pending = count { it.status == TaskStatus.PENDING }
    val inProgress = count { it.status == TaskStatus.IN_PROGRESS }
    val done = count { it.status == TaskStatus.DONE }

    return DashboardStats(
        total = size,
        pending = pending,
        inProgress = inProgress,
        done = done
    )
}

fun TaskStatus.taskStatusToString(): String? {
    return when(this) {
        TaskStatus.PENDING -> "pending"
        TaskStatus.IN_PROGRESS -> "in_progress"
        TaskStatus.DONE -> "done"
        TaskStatus.UNKNOWN -> null
    }
}
