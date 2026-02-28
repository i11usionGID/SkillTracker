package com.i11usion.core.network.model.quiz.attempt

import com.google.gson.annotations.SerializedName
import kotlinx.serialization.Serializable

@Serializable
data class AttemptSubmitRequestDto(
    @SerializedName("answers") val answers: List<AttemptSubmitAnswerDto>
)