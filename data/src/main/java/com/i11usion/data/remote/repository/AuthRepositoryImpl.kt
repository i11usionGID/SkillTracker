package com.i11usion.data.remote.repository

import com.i11usion.core.domain.model.User
import com.i11usion.core.domain.repository.AuthRepository
import com.i11usion.core.domain.repository.SessionRepository
import com.i11usion.core.network.auth.AuthService
import com.i11usion.core.network.model.auth.LoginRequestDto
import com.i11usion.data.mapper.userDtoToUser
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val authService: AuthService,
    private val tokenRepository: SessionRepository
) : AuthRepository {
    override suspend fun login(
        username: String,
        password: String
    ): User {
        val loginRequest = LoginRequestDto(username, password)
        val response = authService.login(loginRequest)
        val user = response.user.userDtoToUser()
        tokenRepository.saveAccessToken(response.accessToken)
        tokenRepository.saveUserInfo(user)
        return user
    }

    override suspend fun logout() {
        authService.logout()
        tokenRepository.clear()
    }

    override suspend fun loadProfile(): User {
        val userDto = authService.me()
        val user = userDto.userDtoToUser()
        tokenRepository.saveUserInfo(user)
        return user
    }
}