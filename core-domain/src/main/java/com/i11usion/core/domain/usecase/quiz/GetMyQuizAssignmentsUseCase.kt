package com.i11usion.core.domain.usecase.quiz

import com.i11usion.core.domain.model.network.AppResult
import com.i11usion.core.domain.model.quiz.MyQuizAssignment
import com.i11usion.core.domain.repository.QuizRepository
import javax.inject.Inject

class GetMyQuizAssignmentsUseCase @Inject constructor(
    private val repository: QuizRepository
) {
    suspend operator fun invoke(): AppResult<List<MyQuizAssignment>> {
        return repository.getMyAssignments()
    }
}