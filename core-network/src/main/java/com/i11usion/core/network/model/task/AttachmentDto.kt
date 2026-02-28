package com.i11usion.core.network.model.task

import com.google.gson.annotations.SerializedName
import kotlinx.serialization.Serializable

@Serializable
data class AttachmentDto(
    @SerializedName("id") val id: Int,
    @SerializedName("task_id") val taskId: Int,
    @SerializedName("uploaded_by_id") val uploadedById: Int,
    @SerializedName("stored_path") val storedPath: String,
    @SerializedName("original_filename") val originalFileName: String,
    @SerializedName("content_type") val contentType: String?,
    @SerializedName("created_at") val createdAt: String
)