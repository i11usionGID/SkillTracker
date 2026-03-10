package com.i11usion.core.domain.usecase.user

import com.i11usion.core.domain.model.network.AppResult
import com.i11usion.core.domain.model.user.User
import com.i11usion.core.domain.repository.UserRepository
import javax.inject.Inject

class GetUsersListUseCase @Inject constructor(
    private val repository: UserRepository
) {
    suspend operator fun invoke(
        page: Int? = null,
        perPage: Int? = null,
        search: String? = null,
        role: String? = null
    ): AppResult<List<User>> {
        return repository.getUserList(page = page, perPage = perPage, search = search, role = role)
    }
}