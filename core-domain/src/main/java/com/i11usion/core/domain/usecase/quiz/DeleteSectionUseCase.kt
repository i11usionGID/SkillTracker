package com.i11usion.core.domain.usecase.quiz

import com.i11usion.core.domain.model.network.AppResult
import com.i11usion.core.domain.repository.QuizRepository
import javax.inject.Inject

class DeleteSectionUseCase @Inject constructor(
    private val repository: QuizRepository
) {
    suspend operator fun invoke(quizId: Int, sectionId: Int): AppResult<Unit> {
        return repository.deleteSection(quizId, sectionId)
    }
}
