package com.i11usion.skilltracker.data.auth.remote.model

import kotlinx.serialization.Serializable

@Serializable
data class LoginRequestDto(
    val username: String,
    val password: String
)
