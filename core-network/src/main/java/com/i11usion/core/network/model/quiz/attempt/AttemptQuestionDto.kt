package com.i11usion.core.network.model.quiz.attempt

import com.google.gson.annotations.SerializedName
import com.i11usion.core.network.model.quiz.QuestionDto
import kotlinx.serialization.Serializable

@Serializable
data class AttemptQuestionDto(
    @SerializedName("attempt_question_id") val attemptQuestionId: Int,
    @SerializedName("question") val question: QuestionDto,
    @SerializedName("section_id") val sectionId: Int? = null,
    @SerializedName("order_index") val orderIndex: Int
)
