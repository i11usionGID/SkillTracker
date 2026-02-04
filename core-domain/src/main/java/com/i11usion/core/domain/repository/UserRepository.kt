package com.i11usion.core.domain.repository

import com.i11usion.core.domain.model.User

interface UserRepository {

    suspend fun getUserList(
        page: Int?,
        perPage: Int?,
        search: String?,
        role: String?
    ): List<User>

    suspend fun deleteUser(userId: Int)
}