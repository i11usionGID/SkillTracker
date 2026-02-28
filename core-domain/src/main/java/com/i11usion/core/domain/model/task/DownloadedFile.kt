package com.i11usion.core.domain.model.task

import java.io.File

data class DownloadedFile(
    val file: File,
    val fileName: String,
    val mimeType: String?
)

