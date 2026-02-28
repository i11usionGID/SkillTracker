package com.i11usion.core.network.model.quiz

import com.google.gson.annotations.SerializedName
import kotlinx.serialization.Serializable

@Serializable
data class QuestionOptionRequestDto(
    @SerializedName("text") val text: String,
    @SerializedName("is_correct") val isCorrect: Boolean = false
)
