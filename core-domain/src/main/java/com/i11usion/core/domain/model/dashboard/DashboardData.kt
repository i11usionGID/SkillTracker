package com.i11usion.core.domain.model.dashboard

import com.i11usion.core.domain.model.task.Task

data class DashboardData(
    val stats: DashboardStats,
    val latest: List<Task>
)