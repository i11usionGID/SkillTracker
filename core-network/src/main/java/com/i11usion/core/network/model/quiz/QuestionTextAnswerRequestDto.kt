package com.i11usion.core.network.model.quiz

import com.google.gson.annotations.SerializedName
import kotlinx.serialization.Serializable

@Serializable
data class QuestionTextAnswerRequestDto(
    @SerializedName("matcher_type") val matcherType: String = "icontains",
    @SerializedName("value") val value: String
)