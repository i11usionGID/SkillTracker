package com.i11usion.skilltracker.domain.model

data class User(
    val id: Int,
    val username: String,
    val name: String,
    val role: Role
)