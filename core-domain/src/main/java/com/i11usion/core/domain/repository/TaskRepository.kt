package com.i11usion.core.domain.repository

import com.i11usion.core.domain.model.task.Attachment
import com.i11usion.core.domain.model.task.Comment
import com.i11usion.core.domain.model.task.Task
import com.i11usion.core.domain.model.task.TaskStatus

interface TaskRepository {
    suspend fun getTaskList(
        page: Int? = null,
        perPage: Int? = null,
        status: String? = null,
        employeeId: Int? = null,
        search: String? = null
    ): List<Task>

    suspend fun getTaskById(taskId: Int): Task

    suspend fun updateProgress(taskId: Int, progress: Int, status: TaskStatus?): Task

    suspend fun getTaskComments(taskId: Int): List<Comment>

    suspend fun getTaskAttachments(taskId: Int): List<Attachment>

    suspend fun deleteTaskComment(taskId: Int, commentId: Int)

    suspend fun deleteTaskAttachment(taskId: Int, attachmentId: Int)

    suspend fun addComment(taskId: Int, text: String): Comment

    suspend fun updateComment(taskId: Int, commentId: Int, text: String): Comment
}