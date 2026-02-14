package com.i11usion.core.network.task

import com.i11usion.core.network.model.task.AttachmentDto
import com.i11usion.core.network.model.task.CommentDto
import com.i11usion.core.network.model.task.CommentRequestDto
import com.i11usion.core.network.model.task.TaskDto
import com.i11usion.core.network.model.task.TaskRequestDto
import com.i11usion.core.network.model.task.UpdateProgressRequestDto
import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.Streaming

interface TaskService {
    @GET("/api/tasks")
    suspend fun getTaskList(
        @Query("page") page: Int? = null,
        @Query("per_page") perPage: Int? = null,
        @Query("status") status: String? = null,
        @Query("employee_id") employeeId: Int? = null,
        @Query("search") search: String? = null
    ): List<TaskDto>

    @GET("/api/tasks/{task_id}")
    suspend fun getTaskById(
        @Path("task_id") taskId: Int
    ): TaskDto

    @PATCH("/api/tasks/{task_id}/progress")
    suspend fun updateProgress(
        @Path("task_id") taskId: Int,
        @Body body: UpdateProgressRequestDto
    ): TaskDto

    @GET("/api/tasks/{task_id}/comments")
    suspend fun getTaskComments(
        @Path("task_id") taskId: Int
    ): List<CommentDto>

    @GET("/api/tasks/{task_id}/attachments")
    suspend fun getTaskAttachments(
        @Path("task_id") taskId: Int
    ): List<AttachmentDto>

    @DELETE("/api/tasks/{task_id}")
    suspend fun deleteTask(
        @Path("task_id") taskId: Int
    )

    @DELETE("/api/tasks/{task_id}/comments/{comment_id}")
    suspend fun deleteTaskComment(
        @Path("task_id") taskId: Int,
        @Path("comment_id") commentId: Int
    )

    @DELETE("/api/tasks/{task_id}/attachments/{attachment_id}")
    suspend fun deleteTaskAttachment(
        @Path("task_id") taskId: Int,
        @Path("attachment_id") attachmentId: Int
    )

    @POST("/api/tasks/{task_id}/comments")
    suspend fun addComment(
        @Path("task_id") taskId: Int,
        @Body body: CommentRequestDto
    ): CommentDto

    @PUT("/api/tasks/{task_id}/comments/{comment_id}")
    suspend fun updateComment(
        @Path("task_id") taskId: Int,
        @Path("comment_id") commentId: Int,
        @Body body: CommentRequestDto
    ): CommentDto

    @POST("/api/tasks")
    suspend fun createTask(
        @Body body: TaskRequestDto
    )

    @Multipart
    @POST("/api/tasks/{task_id}/attachments")
    suspend fun uploadAttachment(
        @Path("task_id") taskId: Int,
        @Part file: MultipartBody.Part
    ): AttachmentDto

    @Streaming
    @GET("/api/tasks/{task_id}/attachments/{attachment_id}/download")
    suspend fun downloadAttachment(
        @Path("task_id") taskId: Int,
        @Path("attachment_id") attachmentId: Int
    ): Response<ResponseBody>
}