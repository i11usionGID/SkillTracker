package com.i11usion.core.domain.usecase.task

import com.i11usion.core.domain.model.network.AppResult
import com.i11usion.core.domain.model.task.Comment
import com.i11usion.core.domain.repository.TaskRepository
import javax.inject.Inject

class GetTaskCommentsUseCase @Inject constructor(
    private val repository: TaskRepository
) {
    suspend operator fun invoke(taskId: Int): AppResult<List<Comment>> {
        return repository.getTaskComments(taskId)
    }
}