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
        assignmentId = assignment.id,
        title = quiz.title,
        description = quiz.description,
        required = assignment.required,
        passPercent = "Проходной балл: ${quiz.passPercent}%",
        timeLimitSeconds = quiz.timeLimitSeconds?.let { formatTime("Время: ", it) },
        attempts = formatAttempts(
            quizMaxAttempts = quiz.maxAttempts,
            assignmentMaxAttempts = assignment.maxAttempts,
            attemptsTaken = assignment.attemptsTaken,
            remainingAttempts = assignment.remainingAttempts
        ),
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
    val hours = seconds / 3600
    val minutes = (seconds % 3600) / 60
    val secs = seconds % 60

    return buildString {
        append(title)

        when {
            hours > 0 -> {
                append(hours).append(" ч.")
                if (minutes > 0) append(" ").append(minutes).append(" мин.")
                if (secs > 0) append(" ").append(secs).append(" сек.")
            }
            minutes > 0 -> {
                append(minutes).append(" мин.")
                if (secs > 0) append(" ").append(secs).append(" сек.")
            }
            else -> {
                append(secs).append(" сек.")
            }
        }
    }
}

private fun formatAttempts(
    quizMaxAttempts: Int?,
    assignmentMaxAttempts: Int?,
    attemptsTaken: Int,
    remainingAttempts: Int?
): String {
    val max = assignmentMaxAttempts ?: quizMaxAttempts

    if (max == null) return "Попытки: без ограничений"

    val remaining = remainingAttempts ?: (max - attemptsTaken).coerceAtLeast(0)

    return "Попытки: $attemptsTaken / $max, осталось $remaining"
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