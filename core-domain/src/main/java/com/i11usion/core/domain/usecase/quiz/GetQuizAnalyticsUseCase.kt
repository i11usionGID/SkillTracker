package com.i11usion.core.domain.usecase.quiz

import com.i11usion.core.domain.model.network.AppResult
import com.i11usion.core.domain.model.quiz.QuizAnalytics
import com.i11usion.core.domain.repository.QuizRepository
import javax.inject.Inject

class GetQuizAnalyticsUseCase @Inject constructor(
    private val repository: QuizRepository
) {
    suspend operator fun invoke(quizId: Int): AppResult<QuizAnalytics> {
        return repository.getQuizAnalytics(quizId)
    }
}
