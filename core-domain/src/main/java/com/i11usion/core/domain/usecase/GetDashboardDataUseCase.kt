package com.i11usion.core.domain.usecase

import com.i11usion.core.domain.mapper.toStats
import com.i11usion.core.domain.model.DashboardData
import com.i11usion.core.domain.repository.TaskRepository
import javax.inject.Inject

class GetDashboardDataUseCase @Inject constructor(
    private val repository: TaskRepository
) {
    
    suspend operator fun invoke(): DashboardData {
        val tasks = repository.getTaskList(page = 1, perPage = 10)
        val stats = tasks.toStats()
        val latest = tasks.sortedByDescending { it.updatedAt }.take(10)

        return DashboardData(stats, latest)
    }
}
