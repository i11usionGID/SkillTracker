package com.i11usion.core.domain.usecase.quiz

import com.i11usion.core.domain.model.quiz.AssignmentDetail
import com.i11usion.core.domain.repository.QuizRepository
import javax.inject.Inject


class GetQuizAssignmentsUseCase @Inject constructor(
    private val repository: QuizRepository
) {
    suspend operator fun invoke(quizId: Int): List<AssignmentDetail> {
        return repository.getQuizAssignments(quizId)
    }
}
