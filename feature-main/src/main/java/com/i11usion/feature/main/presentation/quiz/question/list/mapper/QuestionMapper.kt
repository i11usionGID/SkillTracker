package com.i11usion.feature.main.presentation.quiz.question.list.mapper

import com.i11usion.core.domain.model.quiz.MatcherType
import com.i11usion.core.domain.model.quiz.Question
import com.i11usion.core.domain.model.quiz.QuestionType
import com.i11usion.core.domain.model.quiz.TextAnswer
import com.i11usion.feature.main.presentation.quiz.question.list.model.QuestionUiModel
import com.i11usion.feature.main.presentation.quiz.question.list.model.TextAnswerUiModel

fun Question.domainToUi(): QuestionUiModel {
    return QuestionUiModel(
        id = id,
        text = text,
        questionType = type,
        questionTypeText = when (type) {
            QuestionType.SINGLE -> "Одиночный выбор"
            QuestionType.MULTIPLE -> "Множественный выбор"
            QuestionType.TEXT -> "Текстовый ответ"
        },
        explanation = explanation,
        options = options,
        textAnswers = textAnswers.map {
            it.domainToUi()
        },
    )
}

fun TextAnswer.domainToUi(): TextAnswerUiModel {
    return TextAnswerUiModel(
        matcherTypeText = when (matcherType) {
            MatcherType.EQUALS -> "Точный ответ"
            MatcherType.CONTAINS -> "Содержит"
            MatcherType.ICONTAINS -> "Содержит (без регистра)"
        },
        value = value
    )
}