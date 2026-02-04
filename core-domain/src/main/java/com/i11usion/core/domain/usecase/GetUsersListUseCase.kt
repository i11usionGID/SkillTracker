package com.i11usion.core.domain.usecase

import com.i11usion.core.domain.model.User
import com.i11usion.core.domain.repository.UserRepository
import javax.inject.Inject

class GetUsersListUseCase @Inject constructor(
    private val repository: UserRepository
) {
    suspend operator fun invoke(
        page: Int?,
        perPage: Int?,
        search: String?,
        role: String?
    ): List<User> {
        return repository.getUserList(page = page, perPage = perPage, search = search, role = role)
    }
}