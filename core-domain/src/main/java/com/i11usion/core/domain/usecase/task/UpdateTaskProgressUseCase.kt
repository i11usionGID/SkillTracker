package com.i11usion.core.domain.usecase.task

import com.i11usion.core.domain.model.task.Task
import com.i11usion.core.domain.model.task.TaskStatus
import com.i11usion.core.domain.repository.TaskRepository
import javax.inject.Inject

class UpdateTaskProgressUseCase @Inject constructor(
    private val repository: TaskRepository
) {
    suspend operator fun invoke(taskId: Int, progress: Int, status: TaskStatus?): Task {
        return repository.updateProgress(
            taskId = taskId,
            progress = progress,
            status = status
        )
    }
}