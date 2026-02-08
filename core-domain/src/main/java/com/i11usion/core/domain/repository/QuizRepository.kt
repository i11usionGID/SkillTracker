package com.i11usion.core.domain.repository

import com.i11usion.core.domain.model.MyQuizAssignment
import com.i11usion.core.domain.model.Quiz

interface QuizRepository {
    suspend fun getQuizzes(page: Int?, perPage: Int?, status: String?, search: String?): List<Quiz>
    suspend fun getMyAssignments(): List<MyQuizAssignment>
    suspend fun deleteQuiz(quizId: Int)
}