package com.i11usion.core.network.model.task

import com.google.gson.annotations.SerializedName

data class TaskRequestDto(
    @SerializedName("title") val title: String,
    @SerializedName("description") val description: String?,
    @SerializedName("deadline") val deadline: String?,
    @SerializedName("status") val status: String,
    @SerializedName("progress") val progress: Int,
    @SerializedName("employee_id") val employeeId: Int
)
