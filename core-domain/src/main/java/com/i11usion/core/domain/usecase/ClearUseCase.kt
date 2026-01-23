package com.i11usion.core.domain.usecase

import com.i11usion.core.domain.repository.TokenRepository
import javax.inject.Inject

class ClearUseCase @Inject constructor(
    private val repository: TokenRepository
) {

    operator fun invoke() {
        repository.clear()
    }
}