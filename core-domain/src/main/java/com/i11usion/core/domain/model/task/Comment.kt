package com.i11usion.core.domain.model.task

import com.i11usion.core.domain.model.user.User
import java.time.Instant

data class Comment(
    val id: Int,
    val taskId: Int,
    val userId: Int,
    val text: String,
    val createdAt: Instant,
    val author: User
)