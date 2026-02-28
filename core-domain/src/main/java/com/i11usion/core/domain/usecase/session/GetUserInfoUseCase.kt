package com.i11usion.core.domain.usecase.session

import com.i11usion.core.domain.model.user.User
import com.i11usion.core.domain.repository.SessionRepository
import javax.inject.Inject

class GetUserInfoUseCase @Inject constructor(
    private val repository: SessionRepository
) {
    operator fun invoke(): User? {
        return repository.getUserInfo()
    }
}