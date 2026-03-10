package com.i11usion.data.remote.repository

import com.i11usion.core.domain.model.network.AppResult
import com.i11usion.core.domain.model.user.User
import com.i11usion.core.domain.repository.AuthRepository
import com.i11usion.core.domain.repository.SessionRepository
import com.i11usion.core.network.auth.AuthService
import com.i11usion.core.network.model.auth.LoginRequestDto
import com.i11usion.data.mapper.userDtoToUser
import com.i11usion.data.utils.SafeApiCaller
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val authService: AuthService,
    private val tokenRepository: SessionRepository,
    private val safeApiCaller: SafeApiCaller
) : AuthRepository {
    override suspend fun login(username: String, password: String): AppResult<User> {
        return safeApiCaller.call {
            val request = LoginRequestDto(username, password)
            val response = authService.login(request)

            val user = response.user.userDtoToUser()

            tokenRepository.saveAccessToken(response.accessToken)
            tokenRepository.saveUserInfo(user)

            user
        }
    }

    override suspend fun logout(): AppResult<Unit> {
        val result = safeApiCaller.call { authService.logout() }
        tokenRepository.clear()
        return result
    }

    override suspend fun loadProfile(): AppResult<User> {
        return safeApiCaller.call {
            val userDto = authService.me()
            val user = userDto.userDtoToUser()
            tokenRepository.saveUserInfo(user)
            user
        }
    }
}