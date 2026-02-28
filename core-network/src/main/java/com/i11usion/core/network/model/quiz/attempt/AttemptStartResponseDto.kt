package com.i11usion.core.network.model.quiz.attempt

import com.google.gson.annotations.SerializedName
import kotlinx.serialization.Serializable

@Serializable
data class AttemptStartResponseDto(
    @SerializedName("attempt_id") val attemptId: Int,
    @SerializedName("quiz_id") val quizId: Int,
    @SerializedName("status") val status: String,
    @SerializedName("started_at") val startedAt: String,
    @SerializedName("time_limit_seconds") val timeLimitSeconds: Int? = null,
    @SerializedName("questions") val questions: List<AttemptQuestionDto> = emptyList()
)
