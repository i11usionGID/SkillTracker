package com.i11usion.skilltracker.data.auth.remote.model

data class RegisterRequest(
    val username: String,
    val name: String,
    val role: String,
    val password: String
)

