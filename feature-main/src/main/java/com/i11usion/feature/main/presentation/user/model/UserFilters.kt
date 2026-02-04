package com.i11usion.feature.main.presentation.user.model

import com.i11usion.core.domain.model.Role

data class UserFilters(
    val query: String = "",
    val role: Role? = null
)