package com.i11usion.data.mapper

import com.i11usion.core.domain.model.task.Attachment
import com.i11usion.core.domain.model.task.Comment
import com.i11usion.core.domain.model.task.Task
import com.i11usion.core.domain.model.task.TaskStatus
import com.i11usion.core.network.model.task.AttachmentDto
import com.i11usion.core.network.model.task.CommentDto
import com.i11usion.core.network.model.task.TaskDto
import java.time.Instant

fun TaskDto.taskDtoToTask(): Task =
    Task(
        id = id,
        title = title,
        description = description,
        status = status.toTaskStatus(),
        progress = progress,
        deadline = deadline?.let { Instant.parse(it) },
        createdAt = Instant.parse(createdAt),
        updatedAt = Instant.parse(updatedAt),
        employee = employee.userDtoToUser()
    )

fun CommentDto.dtoToDomain(): Comment = Comment(
        id = id,
        taskId = taskId,
        userId = userId,
        text = text,
        createdAt = Instant.parse(createdAt),
        author = author.userDtoToUser()
    )

fun AttachmentDto.dtoToDomain(): Attachment =
    Attachment(
        id = id,
        taskId = taskId,
        uploadedById = uploadedById,
        storedPath = storedPath,
        originalFileName = originalFileName,
        contentType = contentType,
        createdAt = Instant.parse(createdAt)
    )

fun String.toTaskStatus(): TaskStatus =
    when (this.lowercase()) {
        "pending" -> TaskStatus.PENDING
        "in_progress" -> TaskStatus.IN_PROGRESS
        "done" -> TaskStatus.DONE
        else -> TaskStatus.UNKNOWN
    }

