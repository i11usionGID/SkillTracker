package com.i11usion.data.remote.repository

import com.i11usion.core.domain.model.Task
import com.i11usion.core.domain.repository.TaskRepository
import com.i11usion.core.network.task.TaskService
import com.i11usion.data.mapper.taskDtoToTask
import javax.inject.Inject

class TaskRepositoryImpl @Inject constructor(
    private val taskService: TaskService
) : TaskRepository {
    override suspend fun getTaskList(
        page: Int?,
        perPage: Int?,
        status: String?,
        employeeId: Int?,
        search: String?
    ): List<Task> {
        return taskService.getTaskList(
            page = page,
            perPage = perPage,
            status = status,
            employeeId = employeeId,
            search = search
        ).map {
            it.taskDtoToTask()
        }
    }
}