package com.i11usion.skilltracker.domain.usecase

import com.i11usion.skilltracker.domain.repository.TokenRepository

class GetAccessTokenUseCase(val repository: TokenRepository) {

    operator fun invoke(): String? {
        return repository.getAccessToken()
    }
}