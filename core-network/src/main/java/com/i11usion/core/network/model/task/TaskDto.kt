package com.i11usion.core.network.model.task

import com.google.gson.annotations.SerializedName
import com.i11usion.core.network.model.user.UserDto
import kotlinx.serialization.Serializable

@Serializable
data class TaskDto(
    @SerializedName("id")val id: Int,
    @SerializedName("title")val title: String,
    @SerializedName("description")val description: String?,
    @SerializedName("status")val status: String,
    @SerializedName("progress")val progress: Int,
    @SerializedName("deadline")val deadline: String?,
    @SerializedName("employee_id") val employeeId: Int,
    @SerializedName("created_at") val createdAt: String,
    @SerializedName("updated_at") val updatedAt: String,
    @SerializedName("employee")val employee: UserDto
)