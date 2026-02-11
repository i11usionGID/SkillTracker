package com.i11usion.data.remote.repository

import com.i11usion.core.domain.model.task.Attachment
import com.i11usion.core.domain.model.task.Comment
import com.i11usion.core.domain.model.task.Task
import com.i11usion.core.domain.model.task.TaskStatus
import com.i11usion.core.domain.repository.TaskRepository
import com.i11usion.core.network.model.task.UpdateProgressRequest
import com.i11usion.core.network.task.TaskService
import com.i11usion.data.mapper.dtoToDomain
import com.i11usion.data.mapper.taskDtoToTask
import javax.inject.Inject

class TaskRepositoryImpl @Inject constructor(
    private val taskService: TaskService
) : TaskRepository {
    override suspend fun getTaskList(
        page: Int?,
        perPage: Int?,
        status: String?,
        employeeId: Int?,
        search: String?
    ): List<Task> {
        return taskService.getTaskList(
            page = page,
            perPage = perPage,
            status = status,
            employeeId = employeeId,
            search = search
        ).map {
            it.taskDtoToTask()
        }
    }

    override suspend fun getTaskById(taskId: Int): Task {
        return taskService.getTaskById(taskId).taskDtoToTask()
    }

    override suspend fun updateProgress(
        taskId: Int,
        progress: Int,
        status: TaskStatus?
    ): Task {
        val requestBody = UpdateProgressRequest(
            progress = progress,
            status = when (status) {
                TaskStatus.PENDING -> "pending"
                TaskStatus.IN_PROGRESS -> "in_progress"
                TaskStatus.DONE -> "done"
                else -> null
            }
        )
        return taskService.updateProgress(taskId = taskId, body = requestBody).taskDtoToTask()
    }

    override suspend fun getTaskComments(taskId: Int): List<Comment> {
        return taskService.getTaskComments(taskId).map {
            it.dtoToDomain()
        }
    }

    override suspend fun getTaskAttachments(taskId: Int): List<Attachment> {
        return taskService.getTaskAttachments(taskId).map {
            it.dtoToDomain()
        }
    }

    override suspend fun deleteTaskComment(taskId: Int, commentId: Int) {
        taskService.deleteTaskComment(taskId = taskId, commentId = commentId)
    }

    override suspend fun deleteTaskAttachment(taskId: Int, attachmentId: Int) {
        taskService.deleteTaskAttachment(taskId = taskId, attachmentId = attachmentId)
    }

    override suspend fun addComment(taskId: Int, text: String): Comment {
        return taskService.addComment(taskId = taskId, body = text).dtoToDomain()
    }

    override suspend fun updateComment(
        taskId: Int,
        commentId: Int,
        text: String
    ): Comment {
        return taskService.updateComment(
            taskId = taskId,
            commentId = commentId,
            body = text
        ).dtoToDomain()
    }
}