package com.i11usion.feature.main.presentation.dashboard.model

import com.i11usion.feature.main.presentation.model.TaskUi

data class DashboardDataUi(
    val stats: DashboardStatsUi,
    val latest: List<TaskUi>
)