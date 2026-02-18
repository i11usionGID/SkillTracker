package com.i11usion.feature.main.presentation.quiz.detail.mapper

import com.i11usion.core.domain.model.quiz.AssignmentDetail
import com.i11usion.core.domain.model.quiz.Question
import com.i11usion.core.domain.model.quiz.QuestionSection
import com.i11usion.core.domain.model.quiz.QuestionType
import com.i11usion.core.domain.model.quiz.Quiz
import com.i11usion.core.domain.model.quiz.QuizStatus
import com.i11usion.core.ui.formatter.formatInstantToStringData
import com.i11usion.core.ui.formatter.formatInstantToStringDataNullable
import com.i11usion.feature.main.presentation.quiz.detail.model.AssignmentDetailUiModel
import com.i11usion.feature.main.presentation.quiz.detail.model.QuestionSectionUiModel
import com.i11usion.feature.main.presentation.quiz.detail.model.SectionQuestionsUiModel
import com.i11usion.feature.main.presentation.quiz.list.model.QuizManagerUiModel


fun Quiz.toUiModel(): QuizManagerUiModel =
    QuizManagerUiModel(
        id = id,
        title = title,
        description = description,
        passPercent = "$passPercent",
        timeLimitSeconds = formatTime(timeLimitSeconds),
        maxAttempts = maxAttempts?.toString(),
        status = status,
        statusText = when (status) {
            QuizStatus.DRAFT -> "Черновик"
            QuizStatus.PUBLISHED -> "Опубликован"
        },
        createdAt = formatInstantToStringData(createdAt)
    )

fun QuestionSection.toUiModel(): QuestionSectionUiModel =
    QuestionSectionUiModel(
        id = id,
        quizId = quizId,
        title = title,
        description = description,
        orderIndex = orderIndex,
        pickCount = pickCount,
        questions = questions.map {
            it.tUiModel()
        }
    )

fun Question.tUiModel(): SectionQuestionsUiModel =
    SectionQuestionsUiModel(
        id = id,
        text = text,
        type = QuestionType.toApi(type)
    )

fun AssignmentDetail.toUiModel(): AssignmentDetailUiModel =
    AssignmentDetailUiModel(
        id = assignment.id,
        required = assignment.required,
        allowedUserUsername = assignedUser?.username,
        allowedUserName = assignedUser?.name,
        taskTitle = task?.title,
        dueAt = formatInstantToStringDataNullable(assignment.dueAt),
        maxAttempts = assignment.maxAttempts,
        cooldownHours = assignment.cooldownHours,
        attemptsTaken = assignment.attemptsTaken,
        remainingAttempts = assignment.remainingAttempts
    )

private fun formatTime(seconds: Int?): String? {
    return if (seconds == null) {
        return null
    } else if (seconds < 60) {
        "$seconds сек."
    } else if (seconds % 60 == 0) {
        (seconds / 60).toString() + " мин."
    } else {
        (seconds / 60).toString() + " мин. " + (seconds % 60).toString() + " сек."
    }
}