package com.i11usion.core.domain.usecase

import com.i11usion.core.domain.repository.QuizRepository
import javax.inject.Inject

class DeleteQuizUseCase @Inject constructor(
    private val quizRepository: QuizRepository
) {
    suspend operator fun invoke(quizId: Int) {
        quizRepository.deleteQuiz(quizId)
    }
}