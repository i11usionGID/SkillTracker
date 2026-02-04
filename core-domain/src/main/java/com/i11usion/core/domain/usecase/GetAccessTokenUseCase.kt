package com.i11usion.core.domain.usecase

import com.i11usion.core.domain.repository.SessionRepository
import javax.inject.Inject

class GetAccessTokenUseCase @Inject constructor(
    private val repository: SessionRepository
) {

    operator fun invoke(): String? {
        return repository.getAccessToken()
    }
}