package com.i11usion.core.network.model.quiz

import com.google.gson.annotations.SerializedName
import kotlinx.serialization.Serializable

@Serializable
data class QuestionSectionDto(
    @SerializedName("id") val id: Int,
    @SerializedName("quiz_id") val quizId: Int,
    @SerializedName("title") val title: String,
    @SerializedName("description") val description: String? = null,
    @SerializedName("order_index") val orderIndex: Int,
    @SerializedName("pick_count") val pickCount: Int? = null,
    @SerializedName("questions") val questions: List<QuestionDto> = emptyList()
)
