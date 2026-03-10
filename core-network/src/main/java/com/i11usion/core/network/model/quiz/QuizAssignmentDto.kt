package com.i11usion.core.network.model.quiz

import com.google.gson.annotations.SerializedName
import kotlinx.serialization.Serializable

@Serializable
data class QuizAssignmentDto(
    @SerializedName("id") val id: Int,
    @SerializedName("quiz_id") val quizId: Int,
    @SerializedName("assigned_to_user_id") val assignedToUserId: Int?,
    @SerializedName("task_id") val taskId: Int?,
    @SerializedName("required") val required: Boolean,
    @SerializedName("due_at") val dueAt: String?,
    @SerializedName("max_attempts") val maxAttempts: Int?,
    @SerializedName("cooldown_hours") val cooldownHours: Int,
    @SerializedName("created_at") val createdAt: String,
    @SerializedName("attempts_taken") val attemptsTaken: Int,
    @SerializedName("remaining_attempts") val remainingAttempts: Int?
)

