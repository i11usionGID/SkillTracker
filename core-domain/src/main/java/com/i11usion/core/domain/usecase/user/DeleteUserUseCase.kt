package com.i11usion.core.domain.usecase.user

import com.i11usion.core.domain.model.network.AppResult
import com.i11usion.core.domain.repository.UserRepository
import javax.inject.Inject

class DeleteUserUseCase @Inject constructor(
     private val repository: UserRepository
) {

    suspend operator fun invoke(userId: Int): AppResult<Unit> {
        return repository.deleteUser(userId)
    }
}