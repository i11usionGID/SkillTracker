package com.i11usion.core.domain.usecase.quiz

import com.i11usion.core.domain.model.network.AppResult
import com.i11usion.core.domain.model.quiz.Quiz
import com.i11usion.core.domain.repository.QuizRepository
import javax.inject.Inject

class GetQuizListUseCase @Inject constructor(
    private val repository: QuizRepository
) {
    suspend operator fun invoke(page: Int?, perPage: Int?, status: String?, search: String?): AppResult<List<Quiz>> {
        return repository.getQuizzes(
            page = page,
            perPage = perPage,
            status = status,
            search = search
        )
    }
}