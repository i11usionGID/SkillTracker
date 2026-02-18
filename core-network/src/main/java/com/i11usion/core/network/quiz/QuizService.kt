package com.i11usion.core.network.quiz

import com.i11usion.core.network.model.quiz.AssignmentDetailDto
import com.i11usion.core.network.model.quiz.MyQuizAssignmentDto
import com.i11usion.core.network.model.quiz.QuestionDto
import com.i11usion.core.network.model.quiz.QuestionRequestDto
import com.i11usion.core.network.model.quiz.QuizAnalyticsDto
import com.i11usion.core.network.model.quiz.QuizCreateRequestDto
import com.i11usion.core.network.model.quiz.QuizDto
import com.i11usion.core.network.model.quiz.QuestionSectionDto
import com.i11usion.core.network.model.quiz.attempt.AttemptStartResponseDto
import com.i11usion.core.network.model.quiz.attempt.AttemptSubmitRequestDto
import com.i11usion.core.network.model.quiz.attempt.AttemptSubmitResponseDto
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface QuizService {

    @GET("/api/quizzes")
    suspend fun getQuizzes(
        @Query("page") page: Int? = null,
        @Query("per_page") perPage: Int? = null,
        @Query("status") status: String? = null,
        @Query("search") search: String? = null
    ): List<QuizDto>

    @POST("/api/quizzes")
    suspend fun createQuiz(
        @Body body: QuizCreateRequestDto
    )

    @DELETE("/api/quizzes/{quiz_id}")
    suspend fun deleteQuiz(
        @Path("quiz_id") quizId: Int
    )

    @GET("/api/quizzes/questions")
    suspend fun getQuestions(
        @Query("question_type") type: String? = null
    ): List<QuestionDto>

    @DELETE("/api/quizzes/questions/{question_id}")
    suspend fun deleteQuestion(
        @Path("question_id") questionId: Int
    )

    @POST("/api/quizzes/questions")
    suspend fun createQuestion(
        @Body body: QuestionRequestDto
    )

    @GET("/api/quizzes/questions/{question_id}")
    suspend fun getQuestionById(
        @Path("question_id") questionId: Int
    ): QuestionDto

    @PUT("/api/quizzes/questions/{question_id}")
    suspend fun updateQuestion(
        @Path("question_id") questionId: Int,
        @Body body: QuestionRequestDto
    )

    @GET("/api/quizzes/assignments/my")
    suspend fun getMyAssignments(): List<MyQuizAssignmentDto>

    @POST("/api/quizzes/{quiz_id}/attempts")
    suspend fun startAttempt(
        @Path("quiz_id") quizId: Int
    ): AttemptStartResponseDto

    @POST("/api/quizzes/attempts/{attempt_id}/submit")
    suspend fun submitAttempt(
        @Path("attempt_id") attemptId: Int,
        @Body body: AttemptSubmitRequestDto
    ): AttemptSubmitResponseDto

    @GET("/api/quizzes/{quiz_id}")
    suspend fun getQuizById(
        @Path("quiz_id") quizId: Int
    ): QuizDto

    @GET("/api/quizzes/{quiz_id}/assignment-details")
    suspend fun getQuizAssignments(
        @Path("quiz_id") quizId: Int
    ): List<AssignmentDetailDto>

    @DELETE("/api/quizzes/assignments/{assignment_id}")
    suspend fun deleteAssignment(
        @Path("assignment_id") assignmentId: Int
    )

    @GET("/api/quizzes/{quiz_id}/sections")
    suspend fun getQuestionSections(
        @Path("quiz_id") quizId: Int
    ): List<QuestionSectionDto>

    @DELETE("/api/quizzes/{quiz_id}/sections/{section_id}")
    suspend fun deleteSection(
        @Path("quiz_id") quizId: Int,
        @Path("section_id") sectionId: Int
    )

    @GET("/api/quizzes/{quiz_id}/analytics")
    suspend fun getQuizAnalytics(
        @Path("quiz_id") quizId: Int
    ): QuizAnalyticsDto
}