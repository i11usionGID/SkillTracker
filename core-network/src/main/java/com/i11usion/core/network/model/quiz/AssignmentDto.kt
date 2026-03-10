package com.i11usion.core.network.model.quiz

import com.google.gson.annotations.SerializedName
import kotlinx.serialization.Serializable

@Serializable
data class AssignmentDto(
    @SerializedName("id") val id: Int,
    @SerializedName("quiz_id") val quizId: Int,
    @SerializedName("assigned_to_user_id") val assignedToUserId: Int? = null,
    @SerializedName("task_id") val taskId: Int? = null,
    @SerializedName("required") val required: Boolean,
    @SerializedName("due_at") val dueAt: String? = null,
    @SerializedName("max_attempts") val maxAttempts: Int? = null,
    @SerializedName("cooldown_hours") val cooldownHours: Int,
    @SerializedName("created_at") val createdAt: String,
    @SerializedName("attempts_taken") val attemptsTaken: Int = 0,
    @SerializedName("remaining_attempts") val remainingAttempts: Int? = null
)
