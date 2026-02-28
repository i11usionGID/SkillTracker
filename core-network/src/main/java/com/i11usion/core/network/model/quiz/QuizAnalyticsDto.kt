package com.i11usion.core.network.model.quiz

import com.google.gson.annotations.SerializedName
import kotlinx.serialization.Serializable

@Serializable
data class QuizAnalyticsDto(
    @SerializedName("quiz_id") val quizId: Int,
    @SerializedName("total_assignments") val totalAssignments: Int,
    @SerializedName("total_attempts") val totalAttempts: Int,
    @SerializedName("completed_attempts") val completedAttempts: Int,
    @SerializedName("pass_rate") val passRate: Double
)
