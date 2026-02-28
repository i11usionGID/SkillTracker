package com.i11usion.core.network.model.quiz

import com.google.gson.annotations.SerializedName
import kotlinx.serialization.Serializable

@Serializable
data class QuizDto(
    @SerializedName("id") val id: Int,
    @SerializedName("title") val title: String,
    @SerializedName("description") val description: String?,
    @SerializedName("pass_percent") val passPercent: Int,
    @SerializedName("time_limit_seconds") val timeLimitSeconds: Int?,
    @SerializedName("shuffle_questions") val shuffleQuestions: Boolean,
    @SerializedName("shuffle_answers") val shuffleAnswers: Boolean,
    @SerializedName("allow_review") val allowReview: Boolean,
    @SerializedName("max_attempts") val maxAttempts: Int?,
    @SerializedName("status") val status: String,
    @SerializedName("created_at") val createdAt: String,
    @SerializedName("updated_at") val updatedAt: String
)

