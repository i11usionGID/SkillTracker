package com.i11usion.core.domain.usecase.quiz

import com.i11usion.core.domain.model.network.AppResult
import com.i11usion.core.domain.repository.QuizRepository
import javax.inject.Inject

class GenerateQuizUseCase @Inject constructor(
    private val repository: QuizRepository
) {

    suspend operator fun invoke(topic: String): AppResult<Unit> {
        return repository.generateQuiz(topic)
    }
}