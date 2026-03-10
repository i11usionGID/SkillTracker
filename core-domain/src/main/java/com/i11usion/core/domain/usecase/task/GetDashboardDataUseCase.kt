package com.i11usion.core.domain.usecase.task

import com.i11usion.core.domain.mapper.toStats
import com.i11usion.core.domain.model.dashboard.DashboardData
import com.i11usion.core.domain.model.network.AppResult
import com.i11usion.core.domain.repository.TaskRepository
import javax.inject.Inject

class GetDashboardDataUseCase @Inject constructor(
    private val repository: TaskRepository
) {

    suspend operator fun invoke(): AppResult<DashboardData> {
        val tasks = repository.getTaskList(page = 1, perPage = 10)
        when (tasks) {
            is AppResult.Failure -> {
                return AppResult.Failure(tasks.error)
            }

            is AppResult.Success -> {
                val stats = tasks.data.toStats()
                val latest = tasks.data.sortedByDescending { it.updatedAt }.take(10)

                return AppResult.Success(DashboardData(stats, latest))
            }
        }
    }
}