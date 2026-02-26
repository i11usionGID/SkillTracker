package com.i11usion.core.network.model.quiz

import com.google.gson.annotations.SerializedName
import kotlinx.serialization.Serializable

@Serializable
data class GenerateQuizRequestDto(
    @SerializedName("topic") val topic: String
)