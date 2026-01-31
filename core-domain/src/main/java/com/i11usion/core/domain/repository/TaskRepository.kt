package com.i11usion.core.domain.repository

import com.i11usion.core.domain.model.Task

interface TaskRepository {
    suspend fun getTaskList(
        page: Int? = null,
        perPage: Int? = null,
        status: String? = null,
        employeeId: Int? = null,
        search: String? = null
    ): List<Task>
}