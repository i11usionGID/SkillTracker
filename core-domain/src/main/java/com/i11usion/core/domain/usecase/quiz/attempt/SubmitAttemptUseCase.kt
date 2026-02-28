package com.i11usion.core.domain.usecase.quiz.attempt

import com.i11usion.core.domain.model.quiz.attempt.AttemptSubmit
import com.i11usion.core.domain.model.quiz.attempt.AttemptSubmitRequest
import com.i11usion.core.domain.repository.QuizRepository
import javax.inject.Inject

class SubmitAttemptUseCase @Inject constructor(
    private val repository: QuizRepository
) {
    suspend operator fun invoke(attemptId: Int, request: AttemptSubmitRequest): AttemptSubmit {
        return repository.submitAttempt(attemptId, request)
    }
}