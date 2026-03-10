package com.i11usion.core.network.auth

import com.i11usion.core.network.model.auth.LoginRequestDto
import com.i11usion.core.network.model.auth.LoginResponseDto
import com.i11usion.core.network.model.user.UserDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface AuthService {

    @POST("/api/auth/login")
    suspend fun login(
        @Body request: LoginRequestDto
    ): LoginResponseDto

    @POST("/api/auth/logout")
    suspend fun logout()

    @GET("/api/auth/me")
    suspend fun me(): UserDto
}