package com.i11usion.skilltracker.domain.usecase

import com.i11usion.skilltracker.domain.repository.AuthRepository
import javax.inject.Inject

class LogoutUseCase @Inject constructor(
    val repository: AuthRepository
) {

    suspend operator fun invoke() {
        repository.logout()
    }
}