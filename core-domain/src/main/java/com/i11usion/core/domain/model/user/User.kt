package com.i11usion.core.domain.model.user

import java.time.Instant

data class User(
    val id: Int,
    val username: String,
    val name: String,
    val role: Role,
    val createdAt: Instant
)