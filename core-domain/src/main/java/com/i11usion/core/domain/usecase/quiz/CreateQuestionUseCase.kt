package com.i11usion.core.domain.usecase.quiz

import com.i11usion.core.domain.model.quiz.Question
import com.i11usion.core.domain.repository.QuizRepository
import javax.inject.Inject

class CreateQuestionUseCase @Inject constructor(
    private val repository: QuizRepository
) {
    suspend operator fun invoke(question: Question) {
        repository.createQuestion(question)
    }
}