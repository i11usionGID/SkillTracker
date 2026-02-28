package com.i11usion.core.domain.usecase.quiz

import com.i11usion.core.domain.model.quiz.QuizCreate
import com.i11usion.core.domain.repository.QuizRepository
import javax.inject.Inject

class UpdateQuizUseCase @Inject constructor(
    private val repository: QuizRepository
) {

    suspend operator fun invoke(quizId: Int, quiz: QuizCreate) {
        return repository.updateQuiz(quizId, quiz)
    }
}