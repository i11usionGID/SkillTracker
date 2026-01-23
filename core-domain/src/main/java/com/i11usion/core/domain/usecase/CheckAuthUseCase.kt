package com.i11usion.core.domain.usecase

import com.i11usion.core.domain.repository.AuthRepository
import javax.inject.Inject

class CheckAuthUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(): Boolean =
        runCatching {
            authRepository.loadProfile()
        }.isSuccess
}
