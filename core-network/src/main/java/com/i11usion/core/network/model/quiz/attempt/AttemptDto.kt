package com.i11usion.core.network.model.quiz.attempt

import com.google.gson.annotations.SerializedName
import kotlinx.serialization.Serializable

@Serializable
data class AttemptDto(
    @SerializedName("id") val id: Int,
    @SerializedName("quiz_id") val quizId: Int,
    @SerializedName("assignment_id") val assignmentId: Int?,
    @SerializedName("user_id") val userId: Int,
    @SerializedName("user_name") val userName: String?,
    @SerializedName("status") val status: String,
    @SerializedName("score_percent") val scorePercent: Double?,
    @SerializedName("passed") val passed: Boolean?,
    @SerializedName("started_at") val startedAt: String,
    @SerializedName("finished_at") val finishedAt: String?
)
