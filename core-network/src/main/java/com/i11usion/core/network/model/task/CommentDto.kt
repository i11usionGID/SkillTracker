package com.i11usion.core.network.model.task

import com.google.gson.annotations.SerializedName
import com.i11usion.core.network.model.user.UserDto
import kotlinx.serialization.Serializable

@Serializable
data class CommentDto(
    @SerializedName("id") val id: Int,
    @SerializedName("task_id") val taskId: Int,
    @SerializedName("user_id") val userId: Int,
    @SerializedName("text") val text: String,
    @SerializedName("created_at") val createdAt: String,
    @SerializedName("author") val author: UserDto
)