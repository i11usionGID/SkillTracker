package com.i11usion.core.domain.usecase.quiz.attempt

import com.i11usion.core.domain.model.network.AppResult
import com.i11usion.core.domain.model.quiz.attempt.Attempt
import com.i11usion.core.domain.model.quiz.attempt.AttemptStatus
import com.i11usion.core.domain.repository.QuizRepository
import javax.inject.Inject

class GetQuizAttemptsAnalyticsUseCase @Inject constructor(
    private val repository: QuizRepository
) {

    suspend operator fun invoke(
        quizId: Int,
        userId: Int? = null,
        assignmentId: Int? = null,
        status: AttemptStatus? = null,
        page: Int? = null,
        perPage: Int? = null
    ): AppResult<List<Attempt>> {
        return repository.getQuizAttempts(
            quizId = quizId,
            userId = userId,
            assignmentId = assignmentId,
            status = status,
            page = page,
            perPage = perPage
        )
    }
}