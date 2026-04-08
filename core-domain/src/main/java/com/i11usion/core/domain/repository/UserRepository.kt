package com.i11usion.core.domain.repository

import com.i11usion.core.domain.model.network.AppResult
import com.i11usion.core.domain.model.user.Role
import com.i11usion.core.domain.model.user.User

interface UserRepository {

    suspend fun getUserList(
        page: Int?,
        perPage: Int?,
        search: String?,
        role: String?
    ): AppResult<List<User>>

    suspend fun deleteUser(userId: Int): AppResult<Unit>

    suspend fun createUser(username: String, name: String, role: String, password: String): AppResult<Unit>

    suspend fun changePassword(userId: Int, username: String, name: String, role: Role, password: String): AppResult<Unit>
}