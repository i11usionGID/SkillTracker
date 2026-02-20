package com.i11usion.core.domain.usecase.quiz

import com.i11usion.core.domain.model.quiz.QuestionSection
import com.i11usion.core.domain.repository.QuizRepository
import javax.inject.Inject

class GetQuestionSectionByIdUseCase @Inject constructor(
    private val repository: QuizRepository
) {
    suspend operator fun invoke(quizId: Int, questionSectionId: Int): QuestionSection {
        return repository.getQuestionSectionById(
            quizId = quizId,
            questionSectionId = questionSectionId
        )
    }
}