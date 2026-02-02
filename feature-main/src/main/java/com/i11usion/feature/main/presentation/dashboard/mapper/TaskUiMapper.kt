package com.i11usion.feature.main.presentation.dashboard.mapper

import com.i11usion.core.domain.model.Task
import com.i11usion.core.domain.model.TaskStatus
import com.i11usion.feature.main.presentation.dashboard.model.TaskUiStatus
import com.i11usion.feature.main.presentation.model.TaskUi
import java.time.Instant

fun Task.taskToTaskUi(dateFormatter: (Instant?) -> String): TaskUi {
    val uiStatus: TaskUiStatus
    val uiStatusText: String
    when (status) {
        TaskStatus.PENDING -> {
            uiStatus = TaskUiStatus.PENDING
            uiStatusText = "Ожидает"
        }

        TaskStatus.IN_PROGRESS -> {
            uiStatus = TaskUiStatus.IN_PROGRESS
            uiStatusText = "В работе"
        }

        TaskStatus.DONE -> {
            uiStatus = TaskUiStatus.DONE
            uiStatusText = "Выполнено"
        }

        TaskStatus.UNKNOWN -> {
            throw IllegalStateException("Неизвестный статус задачи")
        }
    }
    return TaskUi(
        id = id,
        title = title,
        description = description,
        status = uiStatus,
        statusText = uiStatusText,
        progress = progress.toString(),
        deadline = dateFormatter(deadline),
        createdAt = dateFormatter(deadline),
        updatedAt = dateFormatter(deadline),
        employee = employee
    )
}