package com.i11usion.skilltracker.domain.usecase

import com.i11usion.skilltracker.domain.model.User
import com.i11usion.skilltracker.domain.repository.AuthRepository
import javax.inject.Inject

class LoginUseCase @Inject constructor(
    private val repository: AuthRepository
) {

    suspend operator fun invoke(username: String, password: String): User {
        return repository.login(username, password)
    }
}