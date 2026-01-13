package com.i11usion.skilltracker.domain.usecase

import com.i11usion.skilltracker.domain.repository.AuthRepository

class LogoutUseCase(val repository: AuthRepository) {

    suspend operator fun invoke() {
        repository.logout()
    }
}