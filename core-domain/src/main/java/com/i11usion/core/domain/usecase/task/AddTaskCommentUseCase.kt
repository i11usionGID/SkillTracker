package com.i11usion.core.domain.usecase.task

import com.i11usion.core.domain.model.task.Comment
import com.i11usion.core.domain.repository.TaskRepository
import javax.inject.Inject

class AddTaskCommentUseCase @Inject constructor(
    private val repository: TaskRepository
) {
    suspend operator fun invoke(taskId: Int, text: String): Comment {
        return repository.addComment(taskId = taskId, text = text)
    }
}