package com.i11usion.data.remote.repository

import com.i11usion.core.domain.model.User
import com.i11usion.core.domain.repository.AuthRepository
import com.i11usion.core.domain.repository.TokenRepository
import com.i11usion.core.network.AuthService
import com.i11usion.core.network.model.LoginRequestDto
import com.i11usion.data.mapper.userDtoToUser
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