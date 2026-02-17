package com.i11usion.core.network.model.quiz.attempt

import com.google.gson.annotations.SerializedName
import kotlinx.serialization.Serializable

@Serializable
data class AttemptAnswerDto(
    @SerializedName("attempt_question_id") val attemptQuestionId: Int,
    @SerializedName("question_id") val questionId: Int,
    @SerializedName("is_correct") val isCorrect: Boolean,
    @SerializedName("selected_option_ids") val selectedOptionIds: List<Int> = emptyList(),
    @SerializedName("text_answer") val textAnswer: String? = null
)
