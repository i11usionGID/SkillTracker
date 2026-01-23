package com.i11usion.data.mapper

import com.i11usion.core.domain.model.Role
import com.i11usion.core.domain.model.User
import com.i11usion.core.network.model.UserDto

fun UserDto.userDtoToUser(): User =
    User(
        id = id,
        username = username,
        name = name,
        role = Role.valueOf(role.uppercase())
    )
