package com.i11usion.core.domain.usecase.task

import com.i11usion.core.domain.model.task.DownloadedFile
import com.i11usion.core.domain.repository.TaskRepository
import javax.inject.Inject

class DownloadTaskAttachmentUseCase @Inject constructor(
    private val repository: TaskRepository
) {
    suspend operator fun invoke(taskId: Int, attachmentId: Int): DownloadedFile {
        return repository.downloadAttachment(taskId, attachmentId)
    }
}