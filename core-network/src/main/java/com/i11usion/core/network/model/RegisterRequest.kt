package com.i11usion.core.network.model

data class RegisterRequest(
    val username: String,
    val name: String,
    val role: String,
    val password: String
)

