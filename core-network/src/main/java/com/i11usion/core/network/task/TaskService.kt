package com.i11usion.core.network.task

import com.i11usion.core.network.model.task.TaskDto
import retrofit2.http.GET
import retrofit2.http.Query

interface TaskService {
    @GET("/api/tasks")
    suspend fun getTaskList(
        @Query("page") page: Int? = null,
        @Query("per_page") perPage: Int? = null,
        @Query("status") status: String? = null,
        @Query("employee_id") employeeId: Int? = null,
        @Query("search") search: String? = null
    ): List<TaskDto>
}