package com.i11usion.core.domain.usecase.task

import com.i11usion.core.domain.model.network.AppResult
import com.i11usion.core.domain.model.task.Comment
import com.i11usion.core.domain.repository.TaskRepository
import javax.inject.Inject

class UpdateTaskCommentUseCase @Inject constructor(
    private val repository: TaskRepository
) {
    suspend operator fun invoke(taskId: Int, commentId: Int, text: String): AppResult<Comment> {
        return repository.updateComment(
            taskId = taskId,
            commentId = commentId,
            text = text
        )
    }
}