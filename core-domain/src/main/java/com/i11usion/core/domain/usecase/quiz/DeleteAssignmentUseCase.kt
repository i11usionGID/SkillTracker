package com.i11usion.core.domain.usecase.quiz

import com.i11usion.core.domain.repository.QuizRepository
import javax.inject.Inject


class DeleteAssignmentUseCase @Inject constructor(
    private val repository: QuizRepository
) {
    suspend operator fun invoke(assignmentId: Int) {
        repository.deleteAssignment(assignmentId)
    }
}
