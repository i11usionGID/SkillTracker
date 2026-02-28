package com.i11usion.core.domain.usecase.auth

import AppError
import com.i11usion.core.domain.model.network.AppResult
import com.i11usion.core.domain.repository.AuthRepository
import javax.inject.Inject

class CheckAuthUseCase @Inject constructor(
    private val repository: AuthRepository
) {
    suspend operator fun invoke(): AppResult<Boolean> {
        return when (val result = repository.loadProfile()) {
            is AppResult.Success -> AppResult.Success(true)

            is AppResult.Failure -> when (result.error) {
                is AppError.Unauthorized,
                AppError.NotFound -> AppResult.Success(false)
                else -> AppResult.Failure(result.error)
            }
        }
    }
}