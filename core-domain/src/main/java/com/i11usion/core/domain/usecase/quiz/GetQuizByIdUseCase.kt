package com.i11usion.core.domain.usecase.quiz

import com.i11usion.core.domain.model.quiz.Quiz
import com.i11usion.core.domain.repository.QuizRepository
import javax.inject.Inject

class GetQuizByIdUseCase @Inject constructor(
    private val repository: QuizRepository
) {
    suspend operator fun invoke(quizId: Int): Quiz {
        return repository.getQuizById(quizId)
    }
}
