package com.i11usion.core.network

import com.i11usion.core.network.model.LoginRequestDto
import com.i11usion.core.network.model.LoginResponse
import com.i11usion.core.network.model.RegisterRequest
import com.i11usion.core.network.model.UserDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface AuthService {

    @POST("/api/auth/register")
    suspend fun register(
        @Body request: RegisterRequest
    )

    @POST("/api/auth/login")
    suspend fun login(
        @Body request: LoginRequestDto
    ): LoginResponse

    @POST("/api/auth/logout")
    suspend fun logout()

    @GET("/api/auth/me")
    suspend fun me(): UserDto
}