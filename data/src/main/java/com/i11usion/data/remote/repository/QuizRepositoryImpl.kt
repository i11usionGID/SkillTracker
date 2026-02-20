package com.i11usion.data.remote.repository

import android.util.Log
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
import com.i11usion.core.network.model.quiz.QuestionSectionRequestDto
import com.i11usion.core.network.quiz.QuizService
import com.i11usion.data.mapper.domainToDto
import com.i11usion.data.mapper.dtoToDomain
import com.i11usion.data.mapper.quizDtoToQuiz
import com.i11usion.data.mapper.toDomain
import java.time.Instant
import javax.inject.Inject

class QuizRepositoryImpl @Inject constructor(
    private val quizService: QuizService
) : QuizRepository {

    override suspend fun getQuizzes(
        page: Int?,
        perPage: Int?,
        status: String?,
        search: String?
    ): List<Quiz> {
        return quizService.getQuizzes(
            page = page,
            perPage = perPage,
            status = status,
            search = search
        ).map {
            it.quizDtoToQuiz()
        }
    }

    override suspend fun createQuiz(quiz: QuizCreate) {
        val body = quiz.domainToDto()
        quizService.createQuiz(body)
    }

    override suspend fun deleteQuiz(quizId: Int) {
        quizService.deleteQuiz(quizId)
    }

    override suspend fun getQuestions(questionType: QuestionType?): List<Question> {
        val type = when (questionType) {
            QuestionType.SINGLE -> "single"
            QuestionType.MULTIPLE -> "multiple"
            QuestionType.TEXT -> "text"
            null -> null
        }
        return quizService.getQuestions(type).map {
            it.dtoToDomain()
        }
    }

    override suspend fun deleteQuestion(questionId: Int) {
        quizService.deleteQuestion(questionId)
    }

    override suspend fun createQuestion(question: Question) {
        val body = question.domainToDto()
        quizService.createQuestion(body)
    }

    override suspend fun getQuestionById(questionId: Int): Question {
        return quizService.getQuestionById(questionId).dtoToDomain()
    }

    override suspend fun updateQuestion(
        questionId: Int,
        question: Question
    ) {
        val body = question.domainToDto()
        quizService.updateQuestion(questionId = questionId, body = body)
    }

    override suspend fun getMyAssignments(): List<MyQuizAssignment> {
        return try {
            Log.d("QuizRepo", "start")
            val dto = quizService.getMyAssignments()
            Log.d("QuizRepo", "dto size=${dto.size}")
            dto.mapIndexed { idx, item ->
                try {
                    item.dtoToDomain()
                } catch (t: Throwable) {
                    Log.e("QuizRepo", "mapping failed at index=$idx item=$item", t)
                    throw t
                }
            }
        } catch (t: Throwable) {
            Log.e("QuizRepo", "getMyAssignments failed: ${t::class.java.name}", t)
            throw t
        }
    }

    override suspend fun startAttempt(quizId: Int): AttemptStart {
        return quizService.startAttempt(quizId).dtoToDomain()
    }

    override suspend fun submitAttempt(
        attemptId: Int,
        request: AttemptSubmitRequest
    ): AttemptSubmit {
        val body = request.domainToDto()
        return quizService.submitAttempt(attemptId = attemptId, body = body).dtoToDomain()
    }

    override suspend fun getAttemptInfoById(attemptId: Int): AttemptRead {
        return quizService.getAttemptById(attemptId).dtoToDomain()
    }

    override suspend fun getQuizById(quizId: Int): Quiz {
        return quizService.getQuizById(quizId).quizDtoToQuiz()
    }

    override suspend fun getQuizAssignments(quizId: Int): List<AssignmentDetail> {
        return quizService.getQuizAssignments(quizId).map { it.dtoToDomain() }
    }

    override suspend fun deleteAssignment(assignmentId: Int) {
        quizService.deleteAssignment(assignmentId)
    }

    override suspend fun getQuestionSections(quizId: Int): List<QuestionSection> {
        return quizService.getQuestionSections(quizId).map { it.dtoToDomain() }
    }

    override suspend fun deleteSection(quizId: Int, sectionId: Int) {
        quizService.deleteSection(quizId = quizId, sectionId = sectionId)
    }

    override suspend fun getQuizAnalytics(quizId: Int): QuizAnalytics {
        return quizService.getQuizAnalytics(quizId).dtoToDomain()
    }

    override suspend fun createQuestionSection(
        quizId: Int,
        title: String,
        description: String?,
        orderIndex: Int,
        pickCount: Int?,
        questionIds: List<Int>
    ) {
        val body = QuestionSectionRequestDto(
            title = title,
            description = description,
            orderIndex = orderIndex,
            pickCount = pickCount,
            questionIds = questionIds
        )
        return quizService.createQuestionSection(quizId, body)
    }

    override suspend fun updateQuestionSection(
        quizId: Int,
        questionSectionId: Int,
        title: String,
        description: String?,
        orderIndex: Int,
        pickCount: Int?,
        questionIds: List<Int>
    ) {
        val body = QuestionSectionRequestDto(
            title = title,
            description = description,
            orderIndex = orderIndex,
            pickCount = pickCount,
            questionIds = questionIds
        )
        return quizService.updateQuestionSection(
            quizId = quizId,
            sectionId = questionSectionId,
            body = body
        )
    }

    override suspend fun getQuestionSectionById(
        quizId: Int,
        questionSectionId: Int
    ): QuestionSection {
        return quizService.getQuestionSectionById(
            quizId = quizId,
            sectionId = questionSectionId
        ).dtoToDomain()
    }

    override suspend fun createAssignment(
        quizId: Int,
        assignedToUserId: Int?,
        taskId: Int?,
        required: Boolean,
        deadline: Instant?,
        maxAttempts: Int?,
        cooldownHours: Int
    ) {
        val body = AssignmentRequestDto(
            assignedToUserId = assignedToUserId,
            taskId = taskId,
            required = required,
            deadline = deadline.toString(),
            maxAttempts = maxAttempts,
            cooldownHours = cooldownHours
        )
        quizService.createAssignment(quizId, body)
    }

    override suspend fun updateQuiz(
        quizId: Int,
        quiz: QuizCreate
    ) {
        val body = quiz.domainToDto()
        quizService.updateQuiz(quizId, body)
    }

    override suspend fun getQuizAttempts(
        quizId: Int,
        userId: Int?,
        assignmentId: Int?,
        status: AttemptStatus?,
        page: Int?,
        perPage: Int?
    ): List<Attempt> {
        return quizService.getQuizAttempts(
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