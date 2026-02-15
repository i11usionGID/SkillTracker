package com.i11usion.feature.main.presentation.quiz.question.list.model

import com.i11usion.core.domain.model.quiz.QuestionOption
import com.i11usion.core.domain.model.quiz.QuestionType

data class QuestionUiModel(
    val id: Int,
    val text: String,
    val questionType: QuestionType,
    val questionTypeText: String,
    val explanation: String?,
    val options: List<QuestionOption>,
    val textAnswers: List<TextAnswerUiModel>
)