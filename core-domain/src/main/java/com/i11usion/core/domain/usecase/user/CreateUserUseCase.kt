package com.i11usion.core.domain.usecase.user

import com.i11usion.core.domain.repository.UserRepository
import javax.inject.Inject


class CreateUserUseCase @Inject constructor(
    private val repository: UserRepository
) {
    suspend operator fun invoke(username: String, name: String, role: String, password: String) {
        repository.createUser(
            username = username,
            name = name,
            role = role,
            password = password
        )
    }
}