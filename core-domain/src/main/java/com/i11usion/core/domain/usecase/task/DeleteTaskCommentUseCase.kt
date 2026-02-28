package com.i11usion.core.domain.usecase.task

import com.i11usion.core.domain.repository.TaskRepository
import javax.inject.Inject

class DeleteTaskCommentUseCase @Inject constructor(
    private val repository: TaskRepository
) {
    suspend operator fun invoke(taskId: Int, commentId: Int) {
        repository.deleteTaskComment(taskId = taskId, commentId = commentId)
    }
}