package com.i11usion.core.domain.usecase.quiz

import com.i11usion.core.domain.repository.QuizRepository
import javax.inject.Inject

class DeleteQuestionUseCase @Inject constructor(
    private val repository: QuizRepository
) {

    suspend operator fun invoke(questionId: Int) {
        repository.deleteQuestion(questionId)
    }
}