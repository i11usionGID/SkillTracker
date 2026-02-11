package com.i11usion.core.domain.repository

import com.i11usion.core.domain.model.user.User

interface AuthRepository {
    suspend fun login(username: String, password: String): User
    suspend fun logout()
    suspend fun loadProfile(): User
}