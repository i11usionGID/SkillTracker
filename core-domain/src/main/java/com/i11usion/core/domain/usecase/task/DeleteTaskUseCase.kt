package com.i11usion.core.domain.usecase.task

import com.i11usion.core.domain.model.network.AppResult
import com.i11usion.core.domain.repository.TaskRepository
import javax.inject.Inject

class DeleteTaskUseCase @Inject constructor(
    private val repository: TaskRepository
) {
    suspend operator fun invoke(taskId: Int): AppResult<Unit> {
        return repository.deleteTask(taskId)
    }
}