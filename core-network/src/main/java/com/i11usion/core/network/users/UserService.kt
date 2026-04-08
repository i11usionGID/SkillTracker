package com.i11usion.core.network.users

import com.i11usion.core.network.model.user.UserDto
import com.i11usion.core.network.model.user.UserRequestDto
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface UserService {

    @GET("/api/users")
    suspend fun getUserList(
        @Query("page") page: Int? = null,
        @Query("per_page") perPage: Int? = null,
        @Query("search") search: String? = null,
        @Query("role") role: String? = null
    ): List<UserDto>

    @DELETE("/api/users/{user_id}")
    suspend fun deleteUser(
        @Path("user_id") userId: Int
    )

    @POST("/api/users")
    suspend fun createUser(
        @Body body: UserRequestDto
    )

    @PUT("/api/users/{user_id}")
    suspend fun updateUser(
        @Path("user_id") userId: Int,
        @Body body: UserRequestDto
    )
}