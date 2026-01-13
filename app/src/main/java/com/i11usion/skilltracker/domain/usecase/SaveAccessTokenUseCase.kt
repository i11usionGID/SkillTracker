package com.i11usion.skilltracker.domain.usecase

import com.i11usion.skilltracker.domain.repository.TokenRepository

class SaveAccessTokenUseCase(val repository: TokenRepository) {

    operator fun invoke(token: String) {
        repository.saveAccessToken(token)
    }
}