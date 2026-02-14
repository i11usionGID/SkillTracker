package com.i11usion.data.remote.repository

import com.i11usion.core.domain.model.user.User
import com.i11usion.core.domain.repository.UserRepository
import com.i11usion.core.network.model.user.UserRequestDto
import com.i11usion.core.network.users.UserService
import com.i11usion.data.mapper.userDtoToUser
import javax.inject.Inject

class UserRepositoryImpl @Inject constructor(
    private val userService: UserService
) : UserRepository {

    override suspend fun getUserList(
        page: Int?,
        perPage: Int?,
        search: String?,
        role: String?
    ): List<User> {
        return userService.getUserList(
            page = page,
            perPage = perPage,
            search = search,
            role = role
        ).map {
            it.userDtoToUser()
        }
    }

    override suspend fun deleteUser(userId: Int) {
        userService.deleteUser(userId)
    }

    override suspend fun createUser(
        username: String,
        name: String,
        role: String,
        password: String
    ) {
        userService.createUser(
            UserRequestDto(
                username = username,
                name = name,
                role = role,
                password = password
            )
        )
    }
}