package com.i11usion.data.mapper

import com.i11usion.core.domain.model.quiz.MyQuizAssignment
import com.i11usion.core.domain.model.quiz.Quiz
import com.i11usion.core.domain.model.quiz.QuizAssignment
import com.i11usion.core.domain.model.quiz.QuizStatus
import com.i11usion.core.network.model.quiz.MyQuizAssignmentDto
import com.i11usion.core.network.model.quiz.QuizAssignmentDto
import com.i11usion.core.network.model.quiz.QuizDto
import java.time.Instant

fun MyQuizAssignmentDto.dtoToDomain(): MyQuizAssignment {
    return MyQuizAssignment(
        assignment = assignment.dtoToDomain(),
        quiz = quiz.quizDtoToQuiz(),
        task = task?.taskDtoToTask()
    )
}

fun QuizDto.quizDtoToQuiz(): Quiz {
    return Quiz(
        id = id,
        title = title,
        description = description,
        passPercent = passPercent,
        timeLimitSeconds = timeLimitSeconds,
        shuffleQuestions = shuffleQuestions,
        shuffleAnswers = shuffleAnswers,
        allowReview = allowReview,
        maxAttempts = maxAttempts,
        status = when (status) {
            "published" -> QuizStatus.PUBLISHED
            else -> QuizStatus.DRAFT
        },
        createdAt = Instant.parse(createdAt),
        updatedAt = Instant.parse(updatedAt)
    )
}

fun QuizAssignmentDto.dtoToDomain(): QuizAssignment {
    return QuizAssignment(
        id = id,
        quizId = quizId,
        assignedToUserId = assignedToUserId,
        taskId = taskId,
        required = required,
        dueAt = dueAt?.let {
            Instant.parse(it)
        },
        maxAttempts = maxAttempts,
        cooldownHours = cooldownHours,
        createdAt = Instant.parse(createdAt),
        attemptsTaken = attemptsTaken,
        remainingAttempts = remainingAttempts
    )
}