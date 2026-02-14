package com.i11usion.core.domain.usecase.task

import android.net.Uri
import com.i11usion.core.domain.model.task.Attachment
import com.i11usion.core.domain.repository.TaskRepository
import javax.inject.Inject

class UploadTaskAttachmentUseCase @Inject constructor(
    private val repository: TaskRepository
) {
    suspend operator fun invoke(taskId: Int, uri: Uri): Attachment {
        return repository.uploadAttachment(taskId, uri)
    }
}