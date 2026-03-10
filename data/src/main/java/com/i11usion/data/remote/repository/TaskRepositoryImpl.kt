package com.i11usion.data.remote.repository

import android.app.Application
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.OpenableColumns
import com.i11usion.core.domain.model.network.AppResult
import com.i11usion.core.domain.model.task.Attachment
import com.i11usion.core.domain.model.task.Comment
import com.i11usion.core.domain.model.task.DownloadedFile
import com.i11usion.core.domain.model.task.Task
import com.i11usion.core.domain.model.task.TaskStatus
import com.i11usion.core.domain.repository.TaskRepository
import com.i11usion.core.network.model.task.CommentRequestDto
import com.i11usion.core.network.model.task.TaskRequestDto
import com.i11usion.core.network.model.task.UpdateProgressRequestDto
import com.i11usion.core.network.task.TaskService
import com.i11usion.data.mapper.dtoToDomain
import com.i11usion.data.mapper.taskDtoToTask
import com.i11usion.data.utils.SafeApiCaller
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.ResponseBody
import retrofit2.HttpException
import java.io.File
import java.io.FileOutputStream
import java.time.Instant
import javax.inject.Inject

class TaskRepositoryImpl @Inject constructor(
    private val application: Application,
    private val taskService: TaskService,
    private val safeApiCaller: SafeApiCaller
) : TaskRepository {
    override suspend fun getTaskList(
        page: Int?,
        perPage: Int?,
        status: String?,
        employeeId: Int?,
        search: String?
    ): AppResult<List<Task>> {
        return safeApiCaller.call {
            taskService.getTaskList(
                page = page,
                perPage = perPage,
                status = status,
                employeeId = employeeId,
                search = search
            ).map {
                it.taskDtoToTask()
            }
        }
    }

    override suspend fun getTaskById(taskId: Int): AppResult<Task> {
        return safeApiCaller.call {
            taskService.getTaskById(taskId).taskDtoToTask()
        }
    }

    override suspend fun updateProgress(
        taskId: Int,
        progress: Int,
        status: TaskStatus?
    ): AppResult<Task> {
        val requestBody = UpdateProgressRequestDto(
            progress = progress,
            status = when (status) {
                TaskStatus.PENDING -> "pending"
                TaskStatus.IN_PROGRESS -> "in_progress"
                TaskStatus.DONE -> "done"
                else -> null
            }
        )
        return safeApiCaller.call {
            taskService.updateProgress(taskId = taskId, body = requestBody).taskDtoToTask()
        }
    }

    override suspend fun getTaskComments(taskId: Int): AppResult<List<Comment>> {
        return safeApiCaller.call {
            taskService.getTaskComments(taskId).map {
                it.dtoToDomain()
            }
        }
    }

    override suspend fun getTaskAttachments(taskId: Int): AppResult<List<Attachment>> {
        return safeApiCaller.call {
            taskService.getTaskAttachments(taskId).map {
                it.dtoToDomain()
            }
        }
    }

    override suspend fun deleteTask(taskId: Int): AppResult<Unit> {
        return safeApiCaller.call {
            taskService.deleteTask(taskId)
        }
    }

    override suspend fun deleteTaskComment(taskId: Int, commentId: Int): AppResult<Unit> {
        return safeApiCaller.call {
            taskService.deleteTaskComment(taskId = taskId, commentId = commentId)
        }
    }

    override suspend fun deleteTaskAttachment(taskId: Int, attachmentId: Int): AppResult<Unit> {
        return safeApiCaller.call {
            taskService.deleteTaskAttachment(taskId = taskId, attachmentId = attachmentId)
        }
    }

    override suspend fun addComment(taskId: Int, text: String): AppResult<Comment> {
        return safeApiCaller.call {
            taskService.addComment(taskId = taskId, body = CommentRequestDto(text)).dtoToDomain()
        }
    }

    override suspend fun updateComment(
        taskId: Int,
        commentId: Int,
        text: String
    ): AppResult<Comment> {
        return safeApiCaller.call {
            taskService.updateComment(
                taskId = taskId,
                commentId = commentId,
                body = CommentRequestDto(text)
            ).dtoToDomain()
        }
    }

    override suspend fun createTask(
        title: String,
        description: String?,
        deadline: Instant?,
        status: TaskStatus,
        progress: Int,
        employeeId: Int
    ): AppResult<Unit> {
        val body = TaskRequestDto(
            title = title,
            description = description,
            deadline = deadline?.toString(),
            status = when (status) {
                TaskStatus.IN_PROGRESS -> "in_progress"
                TaskStatus.DONE -> "done"
                else -> "pending"
            },
            progress = progress,
            employeeId = employeeId
        )
        return safeApiCaller.call {
            taskService.createTask(body)
        }
    }

    override suspend fun uploadAttachment(
        taskId: Int,
        uri: Uri
    ): AppResult<Attachment> {
        val fileName = application.getDisplayName(uri)
        val mime = application.contentResolver.getType(uri)

        val cacheFile = application.copyUriToCache(uri, fileName)

        val requestBody = cacheFile.asRequestBody(mime?.toMediaTypeOrNull())
        val part = MultipartBody.Part.createFormData(
            name = "file",
            filename = fileName,
            body = requestBody
        )

        return safeApiCaller.call {
            val dto = taskService.uploadAttachment(taskId, part)
            cacheFile.delete()
            dto.dtoToDomain()
        }
    }

    override suspend fun downloadAttachment(
        taskId: Int,
        attachmentId: Int
    ): AppResult<DownloadedFile> {

        return safeApiCaller.call {
            val response = taskService.downloadAttachment(taskId, attachmentId)
            if (!response.isSuccessful) {
                throw HttpException(response)
            }

            val body = response.body() ?: error("Empty body")

            val mimeType = response.headers()["Content-Type"]
                ?: body.contentType()?.toString()

            val cd = response.headers()["Content-Disposition"]

            var fileName = parseFileNameFromContentDisposition(cd)

            if (fileName.isNullOrBlank()) {

                val extension = mimeType
                    ?.let {
                        android.webkit.MimeTypeMap.getSingleton()
                            .getExtensionFromMimeType(it.substringBefore(";"))
                    }

                fileName = if (extension != null) {
                    "attachment_$attachmentId.$extension"
                } else {
                    "attachment_$attachmentId"
                }
            }

            val file = application.saveResponseBodyToCache(body, fileName)

            DownloadedFile(
                file = file,
                fileName = fileName,
                mimeType = mimeType
            )
        }
    }

    fun Context.getDisplayName(uri: Uri): String {
        val cursor: Cursor? = contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (it.moveToFirst() && nameIndex >= 0) return it.getString(nameIndex)
        }
        return "file"
    }

    fun Context.copyUriToCache(uri: Uri, fileName: String): File {
        val dst = File(cacheDir, fileName)
        contentResolver.openInputStream(uri)?.use { input ->
            FileOutputStream(dst).use { output ->
                input.copyTo(output)
            }
        } ?: error("Can't open input stream for uri=$uri")
        return dst
    }

    fun parseFileNameFromContentDisposition(cd: String?): String? {
        if (cd.isNullOrBlank()) return null

        val regex = Regex("filename\\*?=(?:UTF-8''|\"?)([^\";]+)")
        return regex.find(cd)?.groupValues?.get(1)
    }

    fun Context.saveResponseBodyToCache(body: ResponseBody, fileName: String): File {
        val file = File(cacheDir, fileName)
        body.byteStream().use { input ->
            FileOutputStream(file).use { output ->
                input.copyTo(output)
            }
        }
        return file
    }
}