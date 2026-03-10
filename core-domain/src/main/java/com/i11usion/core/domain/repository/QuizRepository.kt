package com.i11usion.core.domain.repository

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
import java.time.Instant

interface QuizRepository {
    suspend fun getQuizzes(page: Int?, perPage: Int?, status: String?, search: String?): AppResult<List<Quiz>>
    suspend fun createQuiz(quiz: QuizCreate): AppResult<Unit>
    suspend fun generateQuiz(topic: String): AppResult<Unit>
    suspend fun deleteQuiz(quizId: Int): AppResult<Unit>
    suspend fun getQuizById(quizId: Int): AppResult<Quiz>
    suspend fun getQuestions(questionType: QuestionType?): AppResult<List<Question>>
    suspend fun deleteQuestion(questionId: Int): AppResult<Unit>
    suspend fun createQuestion(question: Question): AppResult<Unit>
    suspend fun getQuestionById(questionId: Int): AppResult<Question>
    suspend fun updateQuestion(questionId: Int, question: Question): AppResult<Unit>
    suspend fun getMyAssignments(): AppResult<List<MyQuizAssignment>>
    suspend fun getQuizAssignments(quizId: Int): AppResult<List<AssignmentDetail>>
    suspend fun deleteAssignment(assignmentId: Int): AppResult<Unit>
    suspend fun getQuestionSections(quizId: Int): AppResult<List<QuestionSection>>
    suspend fun deleteSection(quizId: Int, sectionId: Int): AppResult<Unit>
    suspend fun getQuizAnalytics(quizId: Int): AppResult<QuizAnalytics>
    suspend fun startAttempt(quizId: Int): AppResult<AttemptStart>
    suspend fun submitAttempt(attemptId: Int, request: AttemptSubmitRequest): AppResult<AttemptSubmit>
    suspend fun getAttemptInfoById(attemptId: Int): AppResult<AttemptRead>
    suspend fun createQuestionSection(
        quizId: Int,
        title: String,
        description: String? = null,
        orderIndex: Int,
        pickCount: Int?,
        questionIds: List<Int>
    ): AppResult<Unit>

    suspend fun updateQuestionSection(
        quizId: Int,
        questionSectionId: Int,
        title: String,
        description: String? = null,
        orderIndex: Int,
        pickCount: Int?,
        questionIds: List<Int>
    ): AppResult<Unit>

    suspend fun getQuestionSectionById(quizId: Int, questionSectionId: Int): AppResult<QuestionSection>

    suspend fun createAssignment(
        quizId: Int,
        assignedToUserId: Int?,
        taskId: Int?,
        required: Boolean,
        deadline: Instant?,
        maxAttempts: Int?,
        cooldownHours: Int
    ): AppResult<Unit>

    suspend fun updateQuiz(quizId: Int, quiz: QuizCreate): AppResult<Unit>

    suspend fun getQuizAttempts(
        quizId: Int,
        userId: Int? = null,
        assignmentId: Int? = null,
        status: AttemptStatus? = null,
        page: Int? = null,
        perPage: Int? = null
    ): AppResult<List<Attempt>>
}
