package com.i11usion.data.remote.repository

import android.util.Log
import com.i11usion.core.domain.model.quiz.MyQuizAssignment
import com.i11usion.core.domain.model.quiz.Question
import com.i11usion.core.domain.model.quiz.QuestionType
import com.i11usion.core.domain.model.quiz.Quiz
import com.i11usion.core.domain.repository.QuizRepository
import com.i11usion.core.network.quiz.QuizService
import com.i11usion.data.mapper.dtoToDomain
import com.i11usion.data.mapper.quizDtoToQuiz
import javax.inject.Inject

class QuizRepositoryImpl @Inject constructor(
    private val quizService: QuizService
): QuizRepository {

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
}