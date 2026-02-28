package com.i11usion.core.network.model.task

data class UpdateProgressRequestDto(
    val progress: Int,
    val status: String? = null
)

