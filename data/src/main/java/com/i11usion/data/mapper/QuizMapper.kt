package com.i11usion.data.mapper

import com.i11usion.core.domain.model.quiz.Assignment
import com.i11usion.core.domain.model.quiz.AssignmentDetail
import com.i11usion.core.domain.model.quiz.MatcherType
import com.i11usion.core.domain.model.quiz.MyQuizAssignment
import com.i11usion.core.domain.model.quiz.Question
import com.i11usion.core.domain.model.quiz.QuestionOption
import com.i11usion.core.domain.model.quiz.QuestionSection
import com.i11usion.core.domain.model.quiz.QuestionType
import com.i11usion.core.domain.model.quiz.Quiz
import com.i11usion.core.domain.model.quiz.QuizAnalytics
import com.i11usion.core.domain.model.quiz.QuizAssignment
import com.i11usion.core.domain.model.quiz.QuizCreate
import com.i11usion.core.domain.model.quiz.QuizStatus
import com.i11usion.core.domain.model.quiz.TextAnswer
import com.i11usion.core.domain.model.quiz.attempt.AttemptAnswerResult
import com.i11usion.core.domain.model.quiz.attempt.AttemptQuestion
import com.i11usion.core.domain.model.quiz.attempt.AttemptStart
import com.i11usion.core.domain.model.quiz.attempt.AttemptStatus
import com.i11usion.core.domain.model.quiz.attempt.AttemptSubmit
import com.i11usion.core.domain.model.quiz.attempt.AttemptSubmitAnswer
import com.i11usion.core.domain.model.quiz.attempt.AttemptSubmitRequest
import com.i11usion.core.network.model.quiz.AssignmentDetailDto
import com.i11usion.core.network.model.quiz.AssignmentDto
import com.i11usion.core.network.model.quiz.MyQuizAssignmentDto
import com.i11usion.core.network.model.quiz.QuestionDto
import com.i11usion.core.network.model.quiz.QuestionOptionDto
import com.i11usion.core.network.model.quiz.QuestionOptionRequestDto
import com.i11usion.core.network.model.quiz.QuestionRequestDto
import com.i11usion.core.network.model.quiz.QuestionTextAnswerRequestDto
import com.i11usion.core.network.model.quiz.QuizAssignmentDto
import com.i11usion.core.network.model.quiz.QuizCreateRequestDto
import com.i11usion.core.network.model.quiz.QuizDto
import com.i11usion.core.network.model.quiz.QuestionSectionDto
import com.i11usion.core.network.model.quiz.QuizAnalyticsDto
import com.i11usion.core.network.model.quiz.TextAnswerDto
import com.i11usion.core.network.model.quiz.attempt.AttemptAnswerDto
import com.i11usion.core.network.model.quiz.attempt.AttemptQuestionDto
import com.i11usion.core.network.model.quiz.attempt.AttemptStartResponseDto
import com.i11usion.core.network.model.quiz.attempt.AttemptSubmitAnswerDto
import com.i11usion.core.network.model.quiz.attempt.AttemptSubmitRequestDto
import com.i11usion.core.network.model.quiz.attempt.AttemptSubmitResponseDto
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

fun QuizCreate.domainToDto(): QuizCreateRequestDto =
    QuizCreateRequestDto(
        title = title,
        description = description,
        passPercent = passPercent,
        timeLimitSeconds = timeLimitSeconds,
        shuffleQuestions = shuffleQuestions,
        shuffleAnswers = shuffleAnswers,
        allowReview = allowReview,
        maxAttempts = maxAttempts,
        publish = publish
    )

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

fun QuestionDto.dtoToDomain(): Question {
    return Question(
        id = id,
        text = text,
        type = QuestionType.fromApi(type),
        explanation = explanation,
        options = options.map {
            it.dtoToDomain()
        },
        textAnswers = textAnswers.map {
            it.dtoToDomain()
        }
    )
}

fun QuestionOptionDto.dtoToDomain(): QuestionOption {
    return QuestionOption(
        id = id,
        text = text,
        isCorrect = isCorrect
    )
}

fun TextAnswerDto.dtoToDomain(): TextAnswer {
    return TextAnswer(
        id = id,
        matcherType = MatcherType.fromApi(matcherType),
        value = value
    )
}

fun Question.domainToDto(): QuestionRequestDto =
    QuestionRequestDto(
        text = text,
        type = QuestionType.toApi(type),
        explanation = explanation,
        options = options.map { it.domainToDto() },
        textAnswers = textAnswers.map { it.domainToDto() }
    )

fun QuestionOption.domainToDto(): QuestionOptionRequestDto =
    QuestionOptionRequestDto(
        text = text.trim(),
        isCorrect = isCorrect
    )

fun TextAnswer.domainToDto(): QuestionTextAnswerRequestDto =
    QuestionTextAnswerRequestDto(
        matcherType = MatcherType.toApi(matcherType),
        value = value.trim()
    )

fun AttemptStartResponseDto.dtoToDomain(): AttemptStart =
    AttemptStart(
        attemptId = attemptId,
        quizId = quizId,
        status = status.toAttemptStatus(),
        startedAt = Instant.parse(startedAt),
        timeLimitSeconds = timeLimitSeconds,
        questions = questions.map { it.dtoToDomain() }
    )

fun AttemptQuestionDto.dtoToDomain(): AttemptQuestion =
    AttemptQuestion(
        attemptQuestionId = attemptQuestionId,
        question = question.dtoToDomain(),
        sectionId = sectionId,
        orderIndex = orderIndex
    )

fun AttemptSubmitRequest.domainToDto(): AttemptSubmitRequestDto =
    AttemptSubmitRequestDto(
        answers = answers.map { it.domainToDto() }
    )

fun AttemptSubmitAnswer.domainToDto(): AttemptSubmitAnswerDto =
    AttemptSubmitAnswerDto(
        attemptQuestionId = attemptQuestionId,
        selectedOptionIds = selectedOptionIds,
        textAnswer = textAnswer
    )

fun AttemptSubmitResponseDto.dtoToDomain(): AttemptSubmit =
    AttemptSubmit(
        id = id,
        quizId = quizId,
        userId = userId,
        status = status.toAttemptStatus(),
        scorePercent = scorePercent,
        passed = passed,
        startedAt = Instant.parse(startedAt),
        finishedAt = finishedAt?.let { Instant.parse(it) },
        answers = answers.map { it.dtoToDomain() }
    )

fun AttemptAnswerDto.dtoToDomain(): AttemptAnswerResult =
    AttemptAnswerResult(
        attemptQuestionId = attemptQuestionId,
        questionId = questionId,
        isCorrect = isCorrect,
        selectedOptionIds = selectedOptionIds,
        textAnswer = textAnswer
    )

fun QuestionSectionDto.dtoToDomain(): QuestionSection =
    QuestionSection(
        id = id,
        quizId = quizId,
        title = title,
        description = description,
        orderIndex = orderIndex,
        pickCount = pickCount,
        questions = questions.map {
            it.dtoToDomain()
        }
    )

fun AssignmentDetailDto.dtoToDomain(): AssignmentDetail =
    AssignmentDetail(
        assignment = assignment.dtoToDomain(),
        quiz = quiz.quizDtoToQuiz(),
        task = task?.taskDtoToTask(),
        assignedUser = assignedUser?.userDtoToUser()
    )

fun AssignmentDto.dtoToDomain(): Assignment =
    Assignment(
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

fun QuizAnalyticsDto.dtoToDomain(): QuizAnalytics =
    QuizAnalytics(
        quizId = quizId,
        totalAssignments = totalAssignments,
        totalAttempts = totalAttempts,
        completedAttempts = completedAttempts,
        passRate = passRate
    )

private fun String.toAttemptStatus(): AttemptStatus = when (this.lowercase()) {
    "in_progress" -> AttemptStatus.IN_PROGRESS
    "completed" -> AttemptStatus.COMPLETED
    "expired" -> AttemptStatus.EXPIRED
    else -> AttemptStatus.IN_PROGRESS
}