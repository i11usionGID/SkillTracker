package com.i11usion.core.domain.usecase.task

import com.i11usion.core.domain.model.network.AppResult
import com.i11usion.core.domain.model.task.TaskStatus
import com.i11usion.core.domain.repository.TaskRepository
import java.time.Instant
import javax.inject.Inject

class CreateTaskUseCase @Inject constructor(
    private val repository: TaskRepository
) {

    suspend operator fun invoke(
        title: String,
        description: String?,
        deadline: Instant?,
        status: TaskStatus,
        progress: Int,
        employeeId: Int
    ): AppResult<Unit> {
        return repository.createTask(
            title = title ,
            description = description ,
            deadline = deadline ,
            status = status ,
            progress = progress ,
            employeeId = employeeId
        )
    }
}