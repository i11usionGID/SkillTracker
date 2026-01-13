package com.i11usion.skilltracker.domain.usecase

import com.i11usion.skilltracker.domain.repository.AuthRepository
import javax.inject.Inject

class CheckAuthUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(): Boolean =
        runCatching {
            authRepository.loadProfile()
        }.isSuccess
}
