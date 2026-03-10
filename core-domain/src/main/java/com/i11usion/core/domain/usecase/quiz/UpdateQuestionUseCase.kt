package com.i11usion.core.domain.usecase.quiz

import com.i11usion.core.domain.model.network.AppResult
import com.i11usion.core.domain.model.quiz.Question
import com.i11usion.core.domain.repository.QuizRepository
import javax.inject.Inject

class UpdateQuestionUseCase @Inject constructor(
    private val repository: QuizRepository
) {

    suspend operator fun invoke(questionId: Int, question: Question): AppResult<Unit> {
        return repository.updateQuestion(questionId = questionId, question = question)
    }
}