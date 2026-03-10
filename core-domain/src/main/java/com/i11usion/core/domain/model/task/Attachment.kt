package com.i11usion.core.domain.model.task

import java.time.Instant

data class Attachment(
    val id: Int,
    val taskId: Int,
    val uploadedById: Int,
    val storedPath: String,
    val originalFileName: String,
    val contentType: String?,
    val createdAt: Instant
)
