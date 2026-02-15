package com.i11usion.feature.main.presentation.quiz.list.mapper

import com.i11usion.core.domain.model.quiz.MyQuizAssignment
import com.i11usion.core.domain.model.quiz.Quiz
import com.i11usion.core.domain.model.quiz.QuizStatus
import java.time.Instant

fun Quiz.quizToQuizManagerUi(formatter: (Instant) -> String): com.i11usion.feature.main.presentation.quiz.list.model.QuizManagerUiModel {
    return _root_ide_package_.com.i11usion.feature.main.presentation.quiz.list.model.QuizManagerUiModel(
        id = id,
        title = title,
        description = description,
        passPercent = "$passPercent% для прохождения",
        timeLimitSeconds = timeLimitSeconds?.let { formatTime("Ограничение: ", it) },
        maxAttempts = maxAttempts?.let { "Попыток: $it" },
        status = status,
        statusText = when (status) {
            QuizStatus.DRAFT -> "Черновик"
            QuizStatus.PUBLISHED -> "Опубликован"
        },
        createdAt = formatter(createdAt)
    )
}

fun MyQuizAssignment.quizToQuizEmployeeUi(formatter: (Instant?) -> String): com.i11usion.feature.main.presentation.quiz.list.model.QuizEmployeeUiModel {
    return _root_ide_package_.com.i11usion.feature.main.presentation.quiz.list.model.QuizEmployeeUiModel(
        id = quiz.id,
        title = quiz.title,
        description = quiz.description,
        required = assignment.required,
        passPercent = "Проходной балл: ${quiz.passPercent}%",
        timeLimitSeconds = quiz.timeLimitSeconds?.let { formatTime("Время: ", it) },
        attempts = formatAttempts(quiz.maxAttempts, assignment.attemptsTaken),
        deadline = if (assignment.dueAt != null) {
            "До: " + formatter(assignment.dueAt)
        } else {
            null
        },
        isNoAttempts = isNoAttempts(
            assignment.maxAttempts,
            assignment.attemptsTaken,
            assignment.remainingAttempts
        ),
        isFirstAttempt = assignment.maxAttempts?.let { assignment.attemptsTaken == 0 }
    )
}

private fun formatTime(title: String, seconds: Int): String {
    return if (seconds < 60) {
        title + seconds.toString() + " сек."
    } else if (seconds % 60 == 0) {
        title + (seconds / 60).toString() + " мин."
    } else {
        title + (seconds / 60).toString() + " мин. " + (seconds % 60).toString() + " сек."
    }
}

private fun formatAttempts(maxAttempts: Int?, attemptsTaken: Int): String {
    return "Попытки: " + if (maxAttempts == null) {
        "без ограничений"
    } else {
        "$attemptsTaken / $maxAttempts, осталось ${maxAttempts - attemptsTaken}"
    }
}

private fun isNoAttempts(maxAttempts: Int?, attemptsTaken: Int, attemptsRemaining: Int?): Boolean {
    return if (maxAttempts == 1 && attemptsTaken == 1) {
        true
    } else if (attemptsRemaining == 0) {
        true
    } else if ((maxAttempts?.minus(attemptsTaken)) == 0) {
        true
    } else {
        false
    }
}