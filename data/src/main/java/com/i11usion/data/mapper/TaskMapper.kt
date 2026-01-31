package com.i11usion.data.mapper

import com.i11usion.core.domain.model.Task
import com.i11usion.core.domain.model.TaskStatus
import com.i11usion.core.network.model.task.TaskDto
import java.time.Instant

fun TaskDto.taskDtoToTask(): Task =
    Task(
        id = id,
        title = title,
        description = description,
        status = status.toTaskStatus(),
        progress = progress,
        deadline = deadline.toInstantOrNull(),
        createdAt = createdAt.toInstantOrNull()
            ?: error("Task.createdAt must not be null"),
        updatedAt = updatedAt.toInstantOrNull()
            ?: createdAt.toInstantOrNull(),
        employee = employee.userDtoToUser()
    )

fun String.toTaskStatus(): TaskStatus =
    when (this.lowercase()) {
        "pending" -> TaskStatus.PENDING
        "in_progress" -> TaskStatus.IN_PROGRESS
        "done" -> TaskStatus.DONE
        else -> TaskStatus.UNKNOWN
    }

private fun String?.toInstantOrNull(): Instant? =
    this?.let {
        runCatching { Instant.parse(it) }.getOrNull()
    }


