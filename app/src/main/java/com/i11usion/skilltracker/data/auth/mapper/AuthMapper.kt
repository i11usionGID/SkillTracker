package com.i11usion.skilltracker.data.auth.mapper

import com.i11usion.skilltracker.data.auth.remote.model.UserDto
import com.i11usion.skilltracker.domain.model.Role
import com.i11usion.skilltracker.domain.model.User

fun UserDto.userDtoToUser(): User =
    User(
        id = id,
        username = username,
        name = name,
        role = Role.valueOf(role.uppercase())
    )
