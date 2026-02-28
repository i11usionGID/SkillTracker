package com.i11usion.core.domain.usecase.task

import com.i11usion.core.domain.model.task.Task
import com.i11usion.core.domain.repository.TaskRepository
import javax.inject.Inject

class GetTaskByIdUseCase @Inject constructor(
    private val repository: TaskRepository
) {
    suspend operator fun invoke(taskId: Int): Task {
        return repository.getTaskById(taskId = taskId)
    }
}