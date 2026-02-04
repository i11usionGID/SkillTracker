package com.i11usion.feature.main.presentation.user.model

import com.i11usion.core.domain.model.Role

data class UserUiModel(
    val id: Int,
    val username: String,
    val name: String,
    val shortName: String,
    val role: Role,
    val roleText: String,
    val createdAt: String
)