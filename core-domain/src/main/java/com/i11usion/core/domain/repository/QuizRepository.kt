package com.i11usion.core.domain.repository

import com.i11usion.core.domain.model.quiz.AssignmentDetail
import com.i11usion.core.domain.model.quiz.MyQuizAssignment
import com.i11usion.core.domain.model.quiz.Question
import com.i11usion.core.domain.model.quiz.QuestionSection
import com.i11usion.core.domain.model.quiz.QuestionType
import com.i11usion.core.domain.model.quiz.Quiz
import com.i11usion.core.domain.model.quiz.QuizAnalytics
import com.i11usion.core.domain.model.quiz.QuizCreate
import com.i11usion.core.domain.model.quiz.attempt.AttemptStart
import com.i11usion.core.domain.model.quiz.attempt.AttemptSubmit
import com.i11usion.core.domain.model.quiz.attempt.AttemptSubmitRequest
import java.time.Instant

interface QuizRepository {
    suspend fun getQuizzes(page: Int?, perPage: Int?, status: String?, search: String?): List<Quiz>
    suspend fun createQuiz(quiz: QuizCreate)
    suspend fun deleteQuiz(quizId: Int)
    suspend fun getQuizById(quizId: Int): Quiz
    suspend fun getQuestions(questionType: QuestionType?): List<Question>
    suspend fun deleteQuestion(questionId: Int)
    suspend fun createQuestion(question: Question)
    suspend fun getQuestionById(questionId: Int): Question
    suspend fun updateQuestion(questionId: Int, question: Question)
    suspend fun getMyAssignments(): List<MyQuizAssignment>
    suspend fun getQuizAssignments(quizId: Int): List<AssignmentDetail>
    suspend fun deleteAssignment(assignmentId: Int)
    suspend fun getQuestionSections(quizId: Int): List<QuestionSection>
    suspend fun deleteSection(quizId: Int, sectionId: Int)
    suspend fun getQuizAnalytics(quizId: Int): QuizAnalytics
    suspend fun startAttempt(quizId: Int): AttemptStart
    suspend fun submitAttempt(attemptId: Int, request: AttemptSubmitRequest): AttemptSubmit
    suspend fun createQuestionSection(
        quizId: Int,
        title: String,
        description: String? = null,
        orderIndex: Int,
        pickCount: Int?,
        questionIds: List<Int>
    )

    suspend fun createAssignment(
        quizId: Int,
        assignedToUserId: Int?,
        taskId: Int?,
        required: Boolean,
        deadline: Instant?,
        maxAttempts: Int?,
        cooldownHours: Int
    )
}
