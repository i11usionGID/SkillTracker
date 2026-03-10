package com.i11usion.core.network.model.user

data class UserRequestDto(
    val username: String,
    val name: String,
    val role: String,
    val password: String
)
