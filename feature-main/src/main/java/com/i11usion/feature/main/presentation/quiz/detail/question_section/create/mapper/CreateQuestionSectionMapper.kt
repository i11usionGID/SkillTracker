package com.i11usion.feature.main.presentation.quiz.detail.question_section.create.mapper

import com.i11usion.core.domain.model.quiz.Question
import com.i11usion.core.domain.model.quiz.QuestionType
import com.i11usion.feature.main.presentation.quiz.detail.question_section.create.model.QuestionInQuestionSectionUiModel

fun Question.domainToQuestionInQuestionSectionUiModel(): QuestionInQuestionSectionUiModel =
    QuestionInQuestionSectionUiModel(
        id = id,
        text = text,
        typeText = QuestionType.toApi(type)
    )