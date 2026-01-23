package com.i11usion.core.network.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UserDto(
    val id: Int,
    val username: String,
    val name: String,
    val role: String,
    @SerialName("created_at") val createdAt: String
)
