package com.i11usion.core.domain.usecase.quiz

import com.i11usion.core.domain.repository.QuizRepository
import java.time.Instant
import javax.inject.Inject

class CreateAssignmentUseCase @Inject constructor(
    private val repository: QuizRepository
) {
    suspend operator fun invoke(
        quizId: Int,
        assignedToUserId: Int? = null,
        taskId: Int? = null,
        required: Boolean,
        deadline: Instant? = null,
        maxAttempts: Int? = null,
        cooldownHours: Int
    ) {
        repository.createAssignment(
            quizId = quizId,
            assignedToUserId = assignedToUserId,
            taskId = taskId,
            required = required,
            deadline = deadline,
            maxAttempts = maxAttempts,
            cooldownHours = cooldownHours
        )
    }
}