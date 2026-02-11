package com.i11usion.feature.main.presentation.dashboard.mapper

import com.i11usion.core.domain.model.dashboard.DashboardData
import com.i11usion.core.domain.model.dashboard.DashboardStats
import com.i11usion.core.ui.formatter.formatInstantToStringDataNullable
import com.i11usion.feature.main.presentation.dashboard.model.DashboardDataUi
import com.i11usion.feature.main.presentation.dashboard.model.DashboardStatsUi

fun DashboardData.dashboardDataToDashboardDataUi(): DashboardDataUi {
    return DashboardDataUi(
        stats = stats.toDashboardStateUi(),
        latest = latest.map { it.taskToTaskUi { date -> formatInstantToStringDataNullable(date) } }
    )
}

fun DashboardStats.toDashboardStateUi(): DashboardStatsUi {
    return DashboardStatsUi(
        total = total.toString(),
        inProgress = inProgress.toString() ,
        done = done.toString() ,
        pending = pending.toString()
    )
}