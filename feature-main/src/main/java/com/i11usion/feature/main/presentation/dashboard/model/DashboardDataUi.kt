package com.i11usion.feature.main.presentation.dashboard.model

data class DashboardDataUi(
    val stats: DashboardStatsUi,
    val latest: List<TaskUi>
)