package com.i11usion.core.domain.usecase

import com.i11usion.core.domain.repository.TokenRepository
import javax.inject.Inject

class GetAccessTokenUseCase @Inject constructor(
    val repository: TokenRepository
) {

    operator fun invoke(): String? {
        return repository.getAccessToken()
    }
}