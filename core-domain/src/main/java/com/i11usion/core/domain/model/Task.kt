package com.i11usion.core.domain.model

import java.time.Instant


data class Task(
    val id: Int,
    val title: String,
    val description: String?,
    val status: TaskStatus,
    val progress: Int,
    val deadline: Instant?,
    val createdAt: Instant,
    val updatedAt: Instant?,
    val employee: User
)