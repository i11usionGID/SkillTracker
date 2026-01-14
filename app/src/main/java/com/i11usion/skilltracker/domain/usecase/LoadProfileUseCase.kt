package com.i11usion.skilltracker.domain.usecase

import com.i11usion.skilltracker.domain.model.User
import com.i11usion.skilltracker.domain.repository.AuthRepository
import javax.inject.Inject

class LoadProfileUseCase @Inject constructor(
    val repository: AuthRepository
) {

    suspend operator fun invoke(): User {
        return repository.loadProfile()
    }
}