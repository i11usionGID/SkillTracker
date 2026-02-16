package com.i11usion.core.domain.repository

import com.i11usion.core.domain.model.quiz.MyQuizAssignment
import com.i11usion.core.domain.model.quiz.Question
import com.i11usion.core.domain.model.quiz.QuestionType
import com.i11usion.core.domain.model.quiz.Quiz
import com.i11usion.core.domain.model.quiz.QuizCreate

interface QuizRepository {
    suspend fun getQuizzes(page: Int?, perPage: Int?, status: String?, search: String?): List<Quiz>
    suspend fun createQuiz(quiz: QuizCreate)
    suspend fun deleteQuiz(quizId: Int)
    suspend fun getQuestions(questionType: QuestionType?): List<Question>
    suspend fun deleteQuestion(questionId: Int)
    suspend fun createQuestion(question: Question)
    suspend fun getQuestionById(questionId: Int): Question
    suspend fun updateQuestion(questionId: Int, question: Question)
    suspend fun getMyAssignments(): List<MyQuizAssignment>
}