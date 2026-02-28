package com.i11usion.core.network.model.quiz

import com.google.gson.annotations.SerializedName
import kotlinx.serialization.Serializable

@Serializable
data class QuestionRequestDto(
    @SerializedName("text") val text: String,
    @SerializedName("type") val type: String,
    @SerializedName("explanation") val explanation: String? = null,
    @SerializedName("options") val options: List<QuestionOptionRequestDto> = emptyList(),
    @SerializedName("text_answers") val textAnswers: List<QuestionTextAnswerRequestDto> = emptyList()
)
