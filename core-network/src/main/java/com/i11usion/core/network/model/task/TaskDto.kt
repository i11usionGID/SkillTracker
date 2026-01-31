package com.i11usion.core.network.model.task

import com.google.gson.annotations.SerializedName
import com.i11usion.core.network.model.user.UserDto
import kotlinx.serialization.Serializable

@Serializable
data class TaskDto(
    val id: Int,
    val title: String,
    val description: String?,
    val status: String,
    val progress: Int,
    val deadline: String?,
    @SerializedName("employee_id") val employeeId: Int,
    @SerializedName("created_at") val createdAt: String,
    @SerializedName("updated_at") val updatedAt: String,
    val employee: UserDto
)