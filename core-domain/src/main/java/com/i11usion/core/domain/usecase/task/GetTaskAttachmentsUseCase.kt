package com.i11usion.core.domain.usecase.task

import com.i11usion.core.domain.model.task.Attachment
import com.i11usion.core.domain.repository.TaskRepository
import javax.inject.Inject

class GetTaskAttachmentsUseCase @Inject constructor(
    private val repository: TaskRepository
) {
    suspend operator fun invoke(taskId: Int): List<Attachment> {
        return repository.getTaskAttachments(taskId)
    }
}