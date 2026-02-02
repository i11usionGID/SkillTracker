package com.i11usion.core.network.users

import com.i11usion.core.network.model.user.UserDto
import retrofit2.http.GET
import retrofit2.http.Query

interface UserService {

    @GET("/api/users")
    suspend fun getUserList(
        @Query("page") page: Int? = null,
        @Query("per_page") perPage: Int? = null,
        @Query("search") search: String? = null,
        @Query("role") role: String? = null
    ): List<UserDto>
}