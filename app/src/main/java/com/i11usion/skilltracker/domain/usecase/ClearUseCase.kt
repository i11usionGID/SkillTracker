package com.i11usion.skilltracker.domain.usecase

import com.i11usion.skilltracker.domain.repository.TokenRepository
import javax.inject.Inject

class ClearUseCase @Inject constructor(
    val repository: TokenRepository
) {

    operator fun invoke() {
        repository.clear()
    }
}