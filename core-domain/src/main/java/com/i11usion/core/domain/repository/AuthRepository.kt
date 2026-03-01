package com.i11usion.core.domain.repository

import com.i11usion.core.domain.model.network.AppResult
import com.i11usion.core.domain.model.user.User

interface AuthRepository {
    suspend fun login(username: String, password: String): AppResult<User>
    suspend fun logout(): AppResult<Unit>
    suspend fun loadProfile(): AppResult<User>
}