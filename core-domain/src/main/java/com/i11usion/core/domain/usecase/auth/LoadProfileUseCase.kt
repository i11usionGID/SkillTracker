package com.i11usion.core.domain.usecase.auth

import com.i11usion.core.domain.model.user.User
import com.i11usion.core.domain.repository.AuthRepository
import javax.inject.Inject

class LoadProfileUseCase @Inject constructor(
    private val repository: AuthRepository
) {

    suspend operator fun invoke(): User {
        return repository.loadProfile()
    }
}