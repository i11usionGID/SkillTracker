package com.i11usion.skilltracker.domain.usecase

import com.i11usion.skilltracker.domain.repository.TokenRepository

class ClearUseCase(val repository: TokenRepository) {

    operator fun invoke() {
        repository.clear()
    }
}