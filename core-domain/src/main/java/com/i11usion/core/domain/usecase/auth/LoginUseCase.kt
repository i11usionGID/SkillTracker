package com.i11usion.core.domain.usecase.auth

import com.i11usion.core.domain.model.user.User
import com.i11usion.core.domain.repository.AuthRepository
import javax.inject.Inject

class LoginUseCase @Inject constructor(
    private val repository: AuthRepository
) {

    suspend operator fun invoke(username: String, password: String): User {
        return repository.login(username, password)
    }
}