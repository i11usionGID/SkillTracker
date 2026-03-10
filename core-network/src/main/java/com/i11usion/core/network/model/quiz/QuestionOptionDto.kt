package com.i11usion.core.network.model.quiz

import com.google.gson.annotations.SerializedName
import kotlinx.serialization.Serializable

@Serializable
data class QuestionOptionDto(
    @SerializedName("id") val id: Int,
    @SerializedName("text") val text: String,
    @SerializedName("is_correct") val isCorrect: Boolean
)
