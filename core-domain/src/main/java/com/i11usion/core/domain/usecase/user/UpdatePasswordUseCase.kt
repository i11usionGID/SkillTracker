package com.i11usion.core.domain.usecase.user

import com.i11usion.core.domain.model.network.AppResult
import com.i11usion.core.domain.model.user.Role
import com.i11usion.core.domain.repository.UserRepository
import javax.inject.Inject

class UpdatePasswordUseCase @Inject constructor(
    private val repository: UserRepository
) {

    suspend operator fun invoke(
        userId: Int,
        username: String,
        name: String,
        role: Role,
        password: String
    ): AppResult<Unit> {
        return repository.changePassword(
            userId = userId,
            username = username,
            name = name,
            role = role,
            password = password
        )
    }
}