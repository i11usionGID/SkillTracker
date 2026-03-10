package com.i11usion.data.remote.repository

import com.i11usion.core.domain.model.network.AppResult
import com.i11usion.core.domain.model.quiz.AssignmentDetail
import com.i11usion.core.domain.model.quiz.MyQuizAssignment
import com.i11usion.core.domain.model.quiz.Question
import com.i11usion.core.domain.model.quiz.QuestionSection
import com.i11usion.core.domain.model.quiz.QuestionType
import com.i11usion.core.domain.model.quiz.Quiz
import com.i11usion.core.domain.model.quiz.QuizAnalytics
import com.i11usion.core.domain.model.quiz.QuizCreate
import com.i11usion.core.domain.model.quiz.attempt.Attempt
import com.i11usion.core.domain.model.quiz.attempt.AttemptRead
import com.i11usion.core.domain.model.quiz.attempt.AttemptStart
import com.i11usion.core.domain.model.quiz.attempt.AttemptStatus
import com.i11usion.core.domain.model.quiz.attempt.AttemptSubmit
import com.i11usion.core.domain.model.quiz.attempt.AttemptSubmitRequest
import com.i11usion.core.domain.repository.QuizRepository
import com.i11usion.core.network.model.quiz.AssignmentRequestDto
import com.i11usion.core.network.model.quiz.GenerateQuizRequestDto
import com.i11usion.core.network.model.quiz.QuestionSectionRequestDto
import com.i11usion.core.network.quiz.QuizService
import com.i11usion.data.mapper.domainToDto
import com.i11usion.data.mapper.dtoToDomain
import com.i11usion.data.mapper.quizDtoToQuiz
import com.i11usion.data.mapper.toDomain
import com.i11usion.data.utils.SafeApiCaller
import java.time.Instant
import javax.inject.Inject

class QuizRepositoryImpl @Inject constructor(
    private val quizService: QuizService,
    private val safeApiCaller: SafeApiCaller
) : QuizRepository {

    override suspend fun getQuizzes(
        page: Int?,
        perPage: Int?,
        status: String?,
        search: String?
    ): AppResult<List<Quiz>> {
        return safeApiCaller.call {
            quizService.getQuizzes(
                page = page,
                perPage = perPage,
                status = status,
                search = search
            ).map {
                it.quizDtoToQuiz()
            }
        }
    }

    override suspend fun createQuiz(quiz: QuizCreate): AppResult<Unit> {
        val body = quiz.domainToDto()
        return safeApiCaller.call {
            quizService.createQuiz(body)
        }
    }

    override suspend fun generateQuiz(topic: String): AppResult<Unit> {
        val body = GenerateQuizRequestDto(topic = topic)
        return safeApiCaller.call {
            quizService.generateQuiz(body)
        }
    }

    override suspend fun deleteQuiz(quizId: Int): AppResult<Unit> {
        return safeApiCaller.call {
            quizService.deleteQuiz(quizId)
        }
    }

    override suspend fun getQuestions(questionType: QuestionType?): AppResult<List<Question>> {
        val type = when (questionType) {
            QuestionType.SINGLE -> "single"
            QuestionType.MULTIPLE -> "multiple"
            QuestionType.TEXT -> "text"
            null -> null
        }
        return safeApiCaller.call {
            quizService.getQuestions(type).map {
                it.dtoToDomain()
            }
        }
    }

    override suspend fun deleteQuestion(questionId: Int): AppResult<Unit> {
        return safeApiCaller.call {
            quizService.deleteQuestion(questionId)
        }
    }

    override suspend fun createQuestion(question: Question): AppResult<Unit> {
        val body = question.domainToDto()
        return safeApiCaller.call {
            quizService.createQuestion(body)
        }
    }

    override suspend fun getQuestionById(questionId: Int): AppResult<Question> {
        return safeApiCaller.call {
            quizService.getQuestionById(questionId).dtoToDomain()
        }
    }

    override suspend fun updateQuestion(
        questionId: Int,
        question: Question
    ): AppResult<Unit> {
        val body = question.domainToDto()
        return safeApiCaller.call {
            quizService.updateQuestion(questionId = questionId, body = body)
        }
    }

    override suspend fun getMyAssignments(): AppResult<List<MyQuizAssignment>> {
        return safeApiCaller.call {
            quizService.getMyAssignments().map {
                it.dtoToDomain()
            }
        }
    }

    override suspend fun startAttempt(quizId: Int): AppResult<AttemptStart> {
        return safeApiCaller.call {
            quizService.startAttempt(quizId).dtoToDomain()
        }
    }

    override suspend fun submitAttempt(
        attemptId: Int,
        request: AttemptSubmitRequest
    ): AppResult<AttemptSubmit> {
        val body = request.domainToDto()
        return safeApiCaller.call {
            quizService.submitAttempt(attemptId = attemptId, body = body).dtoToDomain()
        }
    }

    override suspend fun getAttemptInfoById(attemptId: Int): AppResult<AttemptRead> {
        return safeApiCaller.call {
            quizService.getAttemptById(attemptId).dtoToDomain()
        }
    }

    override suspend fun getQuizById(quizId: Int): AppResult<Quiz> {
        return safeApiCaller.call {
            quizService.getQuizById(quizId).quizDtoToQuiz()
        }
    }

    override suspend fun getQuizAssignments(quizId: Int): AppResult<List<AssignmentDetail>> {
        return safeApiCaller.call {
            quizService.getQuizAssignments(quizId).map { it.dtoToDomain() }
        }
    }

    override suspend fun deleteAssignment(assignmentId: Int): AppResult<Unit> {
        return safeApiCaller.call {
            quizService.deleteAssignment(assignmentId)
        }
    }

    override suspend fun getQuestionSections(quizId: Int): AppResult<List<QuestionSection>> {
        return safeApiCaller.call {
            quizService.getQuestionSections(quizId).map { it.dtoToDomain() }
        }
    }

    override suspend fun deleteSection(quizId: Int, sectionId: Int): AppResult<Unit> {
        return safeApiCaller.call {
            quizService.deleteSection(quizId = quizId, sectionId = sectionId)
        }
    }

    override suspend fun getQuizAnalytics(quizId: Int): AppResult<QuizAnalytics> {
        return safeApiCaller.call {
            quizService.getQuizAnalytics(quizId).dtoToDomain()
        }
    }

    override suspend fun createQuestionSection(
        quizId: Int,
        title: String,
        description: String?,
        orderIndex: Int,
        pickCount: Int?,
        questionIds: List<Int>
    ): AppResult<Unit> {
        val body = QuestionSectionRequestDto(
            title = title,
            description = description,
            orderIndex = orderIndex,
            pickCount = pickCount,
            questionIds = questionIds
        )
        return safeApiCaller.call {
            quizService.createQuestionSection(quizId, body)
        }
    }

    override suspend fun updateQuestionSection(
        quizId: Int,
        questionSectionId: Int,
        title: String,
        description: String?,
        orderIndex: Int,
        pickCount: Int?,
        questionIds: List<Int>
    ): AppResult<Unit> {
        val body = QuestionSectionRequestDto(
            title = title,
            description = description,
            orderIndex = orderIndex,
            pickCount = pickCount,
            questionIds = questionIds
        )
        return safeApiCaller.call {
            quizService.updateQuestionSection(
                quizId = quizId,
                sectionId = questionSectionId,
                body = body
            )
        }
    }

    override suspend fun getQuestionSectionById(
        quizId: Int,
        questionSectionId: Int
    ): AppResult<QuestionSection> {
        return safeApiCaller.call {
            quizService.getQuestionSectionById(
                quizId = quizId,
                sectionId = questionSectionId
            ).dtoToDomain()
        }
    }

    override suspend fun createAssignment(
        quizId: Int,
        assignedToUserId: Int?,
        taskId: Int?,
        required: Boolean,
        deadline: Instant?,
        maxAttempts: Int?,
        cooldownHours: Int
    ): AppResult<Unit> {
        val body = AssignmentRequestDto(
            assignedToUserId = assignedToUserId,
            taskId = taskId,
            required = required,
            deadline = deadline.toString(),
            maxAttempts = maxAttempts,
            cooldownHours = cooldownHours
        )
        return safeApiCaller.call {
            quizService.createAssignment(quizId, body)
        }
    }

    override suspend fun updateQuiz(
        quizId: Int,
        quiz: QuizCreate
    ): AppResult<Unit> {
        val body = quiz.domainToDto()
        return safeApiCaller.call {
            quizService.updateQuiz(quizId, body)
        }
    }

    override suspend fun getQuizAttempts(
        quizId: Int,
        userId: Int?,
        assignmentId: Int?,
        status: AttemptStatus?,
        page: Int?,
        perPage: Int?
    ): AppResult<List<Attempt>> {
        return safeApiCaller.call {
            quizService.getQuizAttempts(
                quizId = quizId,
                userId = userId,
                assignmentId = assignmentId,
                status = status.let {
                    when (status) {
                        AttemptStatus.IN_PROGRESS -> "in_progress"
                        AttemptStatus.COMPLETED -> "completed"
                        AttemptStatus.EXPIRED -> "expired"
                        null -> null
                    }
                },
                page = page,
                perPage = perPage
            ).map {
                it.toDomain()
            }
        }
    }
}