package com.i11usion.data.mapper

import com.i11usion.core.domain.model.user.Role
import com.i11usion.core.domain.model.user.User
import com.i11usion.core.network.model.user.UserDto
import java.time.Instant

fun UserDto.userDtoToUser(): User =
    User(
        id = id,
        username = username,
        name = name,
        role = Role.valueOf(role.uppercase()),
        createdAt = Instant.parse(createdAt)
    )
