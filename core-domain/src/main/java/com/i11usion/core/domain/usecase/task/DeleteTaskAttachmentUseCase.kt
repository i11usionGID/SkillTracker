package com.i11usion.core.domain.usecase.task

import com.i11usion.core.domain.repository.TaskRepository
import javax.inject.Inject

class DeleteTaskAttachmentUseCase @Inject constructor(
    private val repository: TaskRepository
) {
    suspend operator fun invoke(taskId: Int, attachmentId: Int) {
        repository.deleteTaskAttachment(taskId = taskId, attachmentId = attachmentId)
    }
}