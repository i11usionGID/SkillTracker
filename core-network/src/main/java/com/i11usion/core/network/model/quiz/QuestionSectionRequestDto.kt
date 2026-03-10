package com.i11usion.core.network.model.quiz

import com.google.gson.annotations.SerializedName
import kotlinx.serialization.Serializable

@Serializable
data class QuestionSectionRequestDto(
    @SerializedName("title") val title: String,
    @SerializedName("description") val description: String?,
    @SerializedName("order_index") val orderIndex: Int,
    @SerializedName("pick_count") val pickCount: Int?,
    @SerializedName("question_ids") val questionIds: List<Int>
)