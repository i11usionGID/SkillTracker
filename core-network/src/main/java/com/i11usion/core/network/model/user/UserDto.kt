package com.i11usion.core.network.model.user

import com.google.gson.annotations.SerializedName
import kotlinx.serialization.Serializable

@Serializable
data class UserDto(
    val id: Int,
    val username: String,
    val name: String,
    val role: String,
    @SerializedName("created_at") val createdAt: String?
)