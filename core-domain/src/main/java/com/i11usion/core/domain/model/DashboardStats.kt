package com.i11usion.core.domain.model

data class DashboardStats(
    val total: Int,
    val inProgress: Int,
    val done: Int,
    val pending: Int
)
