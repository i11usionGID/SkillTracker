package com.i11usion.skilltracker.domain.repository

import com.i11usion.skilltracker.domain.model.User

interface AuthRepository {
    suspend fun login(username: String, password: String): User
    suspend fun logout()
    suspend fun loadProfile(): User
}