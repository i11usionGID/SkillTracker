package com.i11usion.feature.main.presentation.user.list.model

import com.i11usion.core.domain.model.user.Role

data class UserFilters(
    val query: String = "",
    val role: Role? = null
)