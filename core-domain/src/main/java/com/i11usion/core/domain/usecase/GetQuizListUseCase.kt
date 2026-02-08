package com.i11usion.core.domain.usecase

import com.i11usion.core.domain.model.Quiz
import com.i11usion.core.domain.repository.QuizRepository
import javax.inject.Inject

class GetQuizListUseCase @Inject constructor(
    private val repository: QuizRepository
) {
    suspend operator fun invoke(page: Int?, perPage: Int?, status: String?, search: String?): List<Quiz> {
        return repository.getQuizzes(
            page = page,
            perPage = perPage,
            status = status,
            search = search
        )
    }
}