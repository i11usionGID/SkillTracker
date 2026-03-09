package com.i11usion.core.domain.repository

import android.net.Uri
import com.i11usion.core.domain.model.network.AppResult
import com.i11usion.core.domain.model.task.Attachment
import com.i11usion.core.domain.model.task.Comment
import com.i11usion.core.domain.model.task.DownloadedFile
import com.i11usion.core.domain.model.task.Task
import com.i11usion.core.domain.model.task.TaskStatus
import java.time.Instant

interface TaskRepository {
    suspend fun getTaskList(
        page: Int? = null,
        perPage: Int? = null,
        status: String? = null,
        employeeId: Int? = null,
        search: String? = null
    ): AppResult<List<Task>>

    suspend fun getTaskById(taskId: Int): AppResult<Task>

    suspend fun updateProgress(taskId: Int, progress: Int, status: TaskStatus?): AppResult<Task>

    suspend fun getTaskComments(taskId: Int): AppResult<List<Comment>>

    suspend fun getTaskAttachments(taskId: Int): AppResult<List<Attachment>>

    suspend fun deleteTask(taskId: Int): AppResult<Unit>

    suspend fun deleteTaskComment(taskId: Int, commentId: Int): AppResult<Unit>

    suspend fun deleteTaskAttachment(taskId: Int, attachmentId: Int): AppResult<Unit>

    suspend fun addComment(taskId: Int, text: String): AppResult<Comment>

    suspend fun updateComment(taskId: Int, commentId: Int, text: String): AppResult<Comment>

    suspend fun createTask(
        title: String,
        description: String?,
        deadline: Instant?,
        status: TaskStatus,
        progress: Int,
        employeeId: Int,
    ): AppResult<Unit>

    suspend fun uploadAttachment(taskId: Int, uri: Uri): AppResult<Attachment>

    suspend fun downloadAttachment(taskId: Int, attachmentId: Int): AppResult<DownloadedFile>
}