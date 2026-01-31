package com.i11usion.core.network.model.auth

data class RegisterRequest(
    val username: String,
    val name: String,
    val role: String,
    val password: String
)

