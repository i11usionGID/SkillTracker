package com.i11usion.skilltracker.data.auth.remote.repository

import android.util.Log
import com.i11usion.skilltracker.data.auth.mapper.userDtoToUser
import com.i11usion.skilltracker.data.auth.remote.AuthService
import com.i11usion.skilltracker.data.auth.remote.model.LoginRequestDto
import com.i11usion.skilltracker.domain.model.User
import com.i11usion.skilltracker.domain.repository.AuthRepository
import com.i11usion.skilltracker.domain.repository.TokenRepository
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val authService: AuthService,
    private val tokenRepository: TokenRepository
) : AuthRepository {
    override suspend fun login(
        username: String,
        password: String
    ): User {
        val loginRequest = LoginRequestDto(username, password)
        val response = authService.login(loginRequest)
        tokenRepository.saveAccessToken(response.accessToken)
        Log.d("AuthRepositoryImpl", response.toString())
        return response.user.userDtoToUser()
    }

    override suspend fun logout() {
        authService.logout()
        tokenRepository.clear()
    }

    override suspend fun loadProfile(): User {
        val userDto = authService.me()
        return userDto.userDtoToUser()
    }
}