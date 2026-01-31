package com.i11usion.core.domain.model

data class DashboardData(
    val stats: DashboardStats,
    val latest: List<Task>
)