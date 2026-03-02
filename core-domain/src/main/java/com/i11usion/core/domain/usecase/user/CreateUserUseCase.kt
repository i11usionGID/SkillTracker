package com.i11usion.core.domain.usecase.user

import AppError
import com.i11usion.core.domain.model.network.AppResult
import com.i11usion.core.domain.repository.UserRepository
import javax.inject.Inject


class CreateUserUseCase @Inject constructor(
    private val repository: UserRepository
) {
    suspend operator fun invoke(username: String, name: String, role: String, password: String): AppResult<Unit> {
        return repository.createUser(
            username = username,
            name = name,
            role = role,
            password = password
        )
    }
}