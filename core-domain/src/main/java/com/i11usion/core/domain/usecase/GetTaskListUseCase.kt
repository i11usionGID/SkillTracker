package com.i11usion.core.domain.usecase

import com.i11usion.core.domain.mapper.taskStatusToString
import com.i11usion.core.domain.model.Task
import com.i11usion.core.domain.model.TaskStatus
import com.i11usion.core.domain.repository.TaskRepository
import javax.inject.Inject

class GetTaskListUseCase @Inject constructor(
    val repository: TaskRepository
) {
    suspend operator fun invoke(
        status: TaskStatus?,
        employeeId: Int?,
        search: String?
    ): List<Task> {
        return repository.getTaskList(
            status = status?.taskStatusToString(),
            employeeId = employeeId,
            search = search
        )
    }
}