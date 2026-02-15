package com.i11usion.core.network.quiz

import com.i11usion.core.network.model.quiz.MyQuizAssignmentDto
import com.i11usion.core.network.model.quiz.QuestionDto
import com.i11usion.core.network.model.quiz.QuizDto
import retrofit2.http.DELETE
import retrofit2.http.GET
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

    @GET("/api/quizzes/assignments/my")
    suspend fun getMyAssignments(): List<MyQuizAssignmentDto>

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
}