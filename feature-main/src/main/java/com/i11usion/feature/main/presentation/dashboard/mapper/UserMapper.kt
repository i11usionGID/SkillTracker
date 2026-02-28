package com.i11usion.feature.main.presentation.dashboard.mapper

import com.i11usion.core.domain.model.user.Role
import com.i11usion.core.domain.model.user.User
import com.i11usion.feature.main.presentation.user.list.model.UserUiModel
import java.time.Instant

fun User.userToUserUiModel(formatter: (Instant) -> String): UserUiModel {
    return UserUiModel(
        id = id,
        username = username,
        name = name,
        shortName = nameToShortName(name),
        role = role,
        roleText = if (role == Role.MANAGER) {
            "Менеджер"
        } else {
            "Сотрудник"
        },
        createdAt = formatter(createdAt)

    )
}

private fun nameToShortName(name: String): String {
    return if (name.contains(" ")) {
        val words = name.split(" ")
        "${words[0][0].uppercase()}${words[1][0].uppercase()}"
    } else {
        name.substring(0, 1).uppercase()
    }
}