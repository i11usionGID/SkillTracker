package com.i11usion.core.domain.usecase.quiz.attempt

import com.i11usion.core.domain.model.quiz.attempt.AttemptRead
import com.i11usion.core.domain.repository.QuizRepository
import javax.inject.Inject

class GetAttemptInfoByIdUseCase @Inject constructor(
    private val repository: QuizRepository
) {

    suspend operator fun invoke(attemptId: Int): AttemptRead {
        return repository.getAttemptInfoById(attemptId)
    }
}