package com.i11usion.core.domain.usecase.quiz

import com.i11usion.core.domain.model.quiz.QuestionSection
import com.i11usion.core.domain.repository.QuizRepository
import javax.inject.Inject

class GetQuestionSectionsUseCase @Inject constructor(
    private val repository: QuizRepository
) {
    suspend operator fun invoke(quizId: Int): List<QuestionSection> {
        return repository.getQuestionSections(quizId)
    }
}
