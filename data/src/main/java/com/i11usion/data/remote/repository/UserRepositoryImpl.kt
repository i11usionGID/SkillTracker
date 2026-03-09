package com.i11usion.data.remote.repository

import com.i11usion.core.domain.model.network.AppResult
import com.i11usion.core.domain.model.user.User
import com.i11usion.core.domain.repository.UserRepository
import com.i11usion.core.network.model.user.UserRequestDto
import com.i11usion.core.network.users.UserService
import com.i11usion.data.mapper.userDtoToUser
import com.i11usion.data.utils.SafeApiCaller
import javax.inject.Inject

class UserRepositoryImpl @Inject constructor(
    private val userService: UserService,
    private val safeApiCaller: SafeApiCaller
) : UserRepository {

    override suspend fun getUserList(
        page: Int?,
        perPage: Int?,
        search: String?,
        role: String?
    ): AppResult<List<User>> {
        return safeApiCaller.call {
            userService.getUserList(
                page = page,
                perPage = perPage,
                search = search,
                role = role
            ).map {
                it.userDtoToUser()
            }
        }
    }

    override suspend fun deleteUser(userId: Int): AppResult<Unit> {
        return safeApiCaller.call {
            userService.deleteUser(userId)
        }
    }

    override suspend fun createUser(
        username: String,
        name: String,
        role: String,
        password: String
    ): AppResult<Unit> {
        return safeApiCaller.call {
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
}