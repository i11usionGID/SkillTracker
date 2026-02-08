package com.i11usion.core.domain.usecase

import com.i11usion.core.domain.model.MyQuizAssignment
import com.i11usion.core.domain.repository.QuizRepository
import javax.inject.Inject

class GetMyQuizAssignmentsUseCase @Inject constructor(
    private val repository: QuizRepository
) {
    suspend operator fun invoke(): List<MyQuizAssignment> {
        return repository.getMyAssignments()
    }
}