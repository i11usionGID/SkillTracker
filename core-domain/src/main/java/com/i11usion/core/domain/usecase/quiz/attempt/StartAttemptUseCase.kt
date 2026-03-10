package com.i11usion.core.domain.usecase.quiz.attempt

import com.i11usion.core.domain.model.network.AppResult
import com.i11usion.core.domain.model.quiz.attempt.AttemptStart
import com.i11usion.core.domain.repository.QuizRepository
import javax.inject.Inject

class StartAttemptUseCase @Inject constructor(
    private val repository: QuizRepository
) {
    suspend operator fun invoke(quizId: Int): AppResult<AttemptStart> {
        return repository.startAttempt(quizId)
    }
}
