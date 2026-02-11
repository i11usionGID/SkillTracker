package com.i11usion.core.network.model.task

data class UpdateProgressRequest(
    val progress: Int,
    val status: String? = null
)

