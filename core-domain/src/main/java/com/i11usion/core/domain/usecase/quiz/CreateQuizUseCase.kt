package com.i11usion.core.domain.usecase.quiz

import com.i11usion.core.domain.model.quiz.QuizCreate
import com.i11usion.core.domain.repository.QuizRepository
import javax.inject.Inject

class CreateQuizUseCase @Inject constructor(
    private val repository: QuizRepository
) {

    suspend operator fun invoke(quiz: QuizCreate) {
        repository.createQuiz(quiz)
    }
}