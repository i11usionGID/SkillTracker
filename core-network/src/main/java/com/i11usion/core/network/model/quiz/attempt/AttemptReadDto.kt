package com.i11usion.core.network.model.quiz.attempt

import com.google.gson.annotations.SerializedName
import kotlinx.serialization.Serializable

@Serializable
data class AttemptReadDto(
    @SerializedName("id") val id: Int,
    @SerializedName("quiz_id") val quizId: Int,
    @SerializedName("user_id") val userId: Int,
    @SerializedName("status") val status: String,
    @SerializedName("score_percent") val scorePercent: Double? = null,
    @SerializedName("passed") val passed: Boolean? = null,
    @SerializedName("started_at") val startedAt: String,
    @SerializedName("finished_at") val finishedAt: String? = null,
    @SerializedName("answers") val answers: List<AttemptAnswerDto> = emptyList()
)
