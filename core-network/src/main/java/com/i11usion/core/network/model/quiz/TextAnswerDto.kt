package com.i11usion.core.network.model.quiz

import com.google.gson.annotations.SerializedName
import kotlinx.serialization.Serializable

@Serializable
data class TextAnswerDto(
    @SerializedName("id") val id: Int,
    @SerializedName("matcher_type") val matcherType: String,
    @SerializedName("value") val value: String
)
