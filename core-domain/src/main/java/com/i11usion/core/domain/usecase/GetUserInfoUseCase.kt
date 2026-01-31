package com.i11usion.core.domain.usecase

import com.i11usion.core.domain.model.User
import com.i11usion.core.domain.repository.SessionRepository
import javax.inject.Inject

class GetUserInfoUseCase @Inject constructor(
    val repository: SessionRepository
) {
    operator fun invoke(): User? {
        return repository.getUserInfo()
    }
}