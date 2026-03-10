package com.i11usion.core.network.model.auth

import com.google.gson.annotations.SerializedName
import com.i11usion.core.network.model.user.UserDto

data class LoginResponseDto(
    @SerializedName("access_token") val accessToken: String,
    @SerializedName("token_type") val tokenType: String,
    @SerializedName("user") val user: UserDto
)