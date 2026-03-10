package com.i11usion.core.network.model.quiz

import com.google.gson.annotations.SerializedName

data class AssignmentRequestDto(
    @SerializedName("assigned_to_user_id") val assignedToUserId: Int?,
    @SerializedName("task_id") val taskId: Int?,
    @SerializedName("required") val required: Boolean,
    @SerializedName("due_at") val deadline: String?,
    @SerializedName("max_attempts") val maxAttempts: Int?,
    @SerializedName("cooldown_hours") val cooldownHours: Int
)