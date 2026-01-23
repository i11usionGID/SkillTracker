package com.i11usion.core.domain.usecase

import com.i11usion.core.domain.model.User
import com.i11usion.core.domain.repository.AuthRepository
import javax.inject.Inject

class LoadProfileUseCase @Inject constructor(
    val repository: AuthRepository
) {

    suspend operator fun invoke(): User {
        return repository.loadProfile()
    }
}