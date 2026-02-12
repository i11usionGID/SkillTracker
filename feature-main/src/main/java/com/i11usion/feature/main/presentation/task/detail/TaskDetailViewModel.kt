package com.i11usion.feature.main.presentation.task.detail

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.i11usion.core.domain.model.task.TaskStatus
import com.i11usion.core.domain.model.user.User
import com.i11usion.core.domain.usecase.session.GetUserInfoUseCase
import com.i11usion.core.domain.usecase.task.AddTaskCommentUseCase
import com.i11usion.core.domain.usecase.task.DeleteTaskAttachmentUseCase
import com.i11usion.core.domain.usecase.task.DeleteTaskCommentUseCase
import com.i11usion.core.domain.usecase.task.GetTaskAttachmentsUseCase
import com.i11usion.core.domain.usecase.task.GetTaskByIdUseCase
import com.i11usion.core.domain.usecase.task.GetTaskCommentsUseCase
import com.i11usion.core.domain.usecase.task.UpdateTaskCommentUseCase
import com.i11usion.core.domain.usecase.task.UpdateTaskProgressUseCase
import com.i11usion.core.ui.formatter.formatInstantToStringDataNullable
import com.i11usion.feature.main.presentation.dashboard.mapper.taskToTaskUi
import com.i11usion.feature.main.presentation.model.TaskUi
import com.i11usion.feature.main.presentation.task.detail.mapper.domainToUi
import com.i11usion.feature.main.presentation.task.detail.model.AttachmentUiModel
import com.i11usion.feature.main.presentation.task.detail.model.CommentBlock
import com.i11usion.feature.main.presentation.task.detail.model.EditableComment
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

class TaskDetailViewModel @Inject constructor(
    private val getUserInfoUseCase: GetUserInfoUseCase,
    private val getTaskByIdUseCase: GetTaskByIdUseCase,
    private val updateTaskProgressUseCase: UpdateTaskProgressUseCase,
    private val getTaskCommentsUseCase: GetTaskCommentsUseCase,
    private val getTaskAttachmentsUseCase: GetTaskAttachmentsUseCase,
    private val deleteTaskCommentUseCase: DeleteTaskCommentUseCase,
    private val deleteTaskAttachmentUseCase: DeleteTaskAttachmentUseCase,
    private val addTaskCommentUseCase: AddTaskCommentUseCase,
    private val updateTaskCommentUseCase: UpdateTaskCommentUseCase
) : ViewModel() {

    private var currentTaskId: Int? = null

    private val user = MutableStateFlow<User?>(null)
    private val task = MutableStateFlow<TaskUi?>(null)

    private val attachments = MutableStateFlow<List<AttachmentUiModel>>(emptyList())

    private val commentsBlock = MutableStateFlow(
        CommentBlock(
            query = "",
            isLoading = false,
            commentList = emptyList(),
            editableComment = null
        )
    )

    private val _state = MutableStateFlow<TaskDetailUiState>(TaskDetailUiState.Initial)
    val state = _state.asStateFlow()

    fun processCommand(command: TaskDetailCommand) {
        when (command) {
            is TaskDetailCommand.Init -> init(command.taskId)

            is TaskDetailCommand.UpdateProgress -> updateProgress(
                taskId = command.taskId,
                newProgress = command.newProgress
            )

            is TaskDetailCommand.InputCommentQuery -> {
                commentsBlock.update { it.copy(query = command.query) }
                publishContentIfReady()
            }

            TaskDetailCommand.AddComment -> addComment()

            is TaskDetailCommand.DeleteComment -> deleteComment(command.commentId)

            is TaskDetailCommand.StartEditComment -> startEditComment(command.commentId)

            is TaskDetailCommand.EditCommentTextChanged -> editCommentTextChanged(command.text)

            TaskDetailCommand.CancelEditComment -> cancelEditComment()

            TaskDetailCommand.FinishEditComment -> finishEditComment()

            is TaskDetailCommand.UploadAttachment -> uploadAttachment(command.taskId, command.uri)

            is TaskDetailCommand.DeleteAttachment -> {}

            is TaskDetailCommand.DownloadAttachment -> {}

            is TaskDetailCommand.DeleteTask -> {}

            TaskDetailCommand.Retry -> retry()
        }
    }

    private fun init(taskId: Int) {
        if (currentTaskId == taskId && _state.value is TaskDetailUiState.Content) return
        currentTaskId = taskId

        _state.value = TaskDetailUiState.Loading

        viewModelScope.launch {
            try {
                val u = getUserInfoUseCase()
                user.value = u

                val domainTask = getTaskByIdUseCase(taskId)
                task.value = domainTask.taskToTaskUi(
                    dateFormatter = { date ->
                        formatInstantToStringDataNullable(date)
                    }
                )

                commentsBlock.update { it.copy(isLoading = true) }
                publishContentIfReady()

                val domainComments = getTaskCommentsUseCase(taskId)
                commentsBlock.value = commentsBlock.value.copy(
                    isLoading = false,
                    commentList = domainComments.map { it.domainToUi() }
                )

                val attachmentList = getTaskAttachmentsUseCase(taskId)
                attachments.value = attachmentList.map {
                    it.domainToUi()
                }

                publishContentIfReady()
            } catch (t: Throwable) {
                _state.value = TaskDetailUiState.Error(t.message ?: "Unknown error")
            }
        }
    }

    private fun retry() {
        val id = currentTaskId ?: return
        init(id)
    }

    private fun publishContentIfReady() {
        val u = user.value ?: return
        val t = task.value ?: return

        _state.value = TaskDetailUiState.Content(
            user = u,
            task = t,
            attachments = attachments.value,
            comments = commentsBlock.value
        )
    }

    private fun updateProgress(taskId: Int, newProgress: Int) {
        viewModelScope.launch {
            try {
                val status = progressToStatus(newProgress)

                val updatedTask = updateTaskProgressUseCase(
                    taskId = taskId,
                    progress = newProgress,
                    status = status
                )

                task.value = updatedTask.taskToTaskUi(
                    dateFormatter = { date ->
                        formatInstantToStringDataNullable(date)
                    }
                )
                publishContentIfReady()
            } catch (t: Throwable) {
                _state.value = TaskDetailUiState.Error(t.message ?: "Failed to update progress")
            }
        }
    }

    private fun progressToStatus(progress: Int): TaskStatus {
        return when {
            progress <= 0 -> TaskStatus.PENDING
            progress in 1..99 -> TaskStatus.IN_PROGRESS
            else -> TaskStatus.DONE
        }
    }

    private fun addComment() {
        val content = _state.value as? TaskDetailUiState.Content ?: return
        val taskId = content.task.id
        val text = commentsBlock.value.query.trim()
        if (text.isBlank()) return

        viewModelScope.launch {
            try {
                commentsBlock.update { it.copy(isLoading = true) }
                publishContentIfReady()

                val created = addTaskCommentUseCase(taskId = taskId, text = text)

                val newList = commentsBlock.value.commentList + created.domainToUi()

                commentsBlock.value = commentsBlock.value.copy(
                    isLoading = false,
                    query = "",
                    commentList = newList
                )
                publishContentIfReady()
            } catch (t: Throwable) {
                commentsBlock.update { it.copy(isLoading = false) }
                _state.value = TaskDetailUiState.Error(t.message ?: "Failed to add comment")
            }
        }
    }

    private fun deleteComment(commentId: Int) {
        val content = _state.value as? TaskDetailUiState.Content ?: return
        val taskId = content.task.id

        viewModelScope.launch {
            val old = commentsBlock.value

            commentsBlock.value = old.copy(
                commentList = old.commentList.filterNot { it.id == commentId },
                editableComment = if (old.editableComment?.commentId == commentId) null else old.editableComment
            )
            publishContentIfReady()

            try {
                deleteTaskCommentUseCase(taskId = taskId, commentId = commentId)
            } catch (t: Throwable) {
                commentsBlock.value = old
                publishContentIfReady()
                _state.value = TaskDetailUiState.Error(t.message ?: "Failed to delete comment")
            }
        }
    }

    private fun startEditComment(commentId: Int) {
        val current = commentsBlock.value
        val comment = current.commentList.firstOrNull { it.id == commentId } ?: return

        commentsBlock.value = current.copy(
            editableComment = EditableComment(commentId = commentId, text = comment.text)
        )
        publishContentIfReady()
    }

    private fun editCommentTextChanged(text: String) {
        val current = commentsBlock.value
        val editable = current.editableComment ?: return
        commentsBlock.value = current.copy(
            editableComment = editable.copy(text = text)
        )
        publishContentIfReady()
    }

    private fun cancelEditComment() {
        commentsBlock.update { it.copy(editableComment = null) }
        publishContentIfReady()
    }

    private fun finishEditComment() {
        val content = _state.value as? TaskDetailUiState.Content ?: return
        val taskId = content.task.id

        val current = commentsBlock.value
        val editable = current.editableComment ?: return
        val newText = editable.text.trim()
        if (newText.isBlank()) return

        viewModelScope.launch {
            val old = commentsBlock.value

            val updatedList = old.commentList.map {
                if (it.id == editable.commentId) it.copy(text = newText) else it
            }
            commentsBlock.value = old.copy(commentList = updatedList, isLoading = true)
            publishContentIfReady()

            try {
                updateTaskCommentUseCase(
                    taskId = taskId,
                    commentId = editable.commentId,
                    text = newText
                )

                commentsBlock.value = commentsBlock.value.copy(
                    isLoading = false,
                    editableComment = null
                )
                publishContentIfReady()
            } catch (t: Throwable) {
                commentsBlock.value = old.copy(isLoading = false)
                publishContentIfReady()
                _state.value = TaskDetailUiState.Error(t.message ?: "Failed to update comment")
            }
        }
    }

    private fun uploadAttachment(taskId: Int, uri: Uri) {
    }

}

sealed interface TaskDetailCommand {
    data class Init(val taskId: Int) : TaskDetailCommand
    data class DeleteTask(val taskId: Int) : TaskDetailCommand
    data class UpdateProgress(val newProgress: Int, val taskId: Int) : TaskDetailCommand
    data class UploadAttachment(val taskId: Int, val uri: Uri) : TaskDetailCommand
    data class DeleteAttachment(val attachmentId: Int) : TaskDetailCommand
    data class DownloadAttachment(val attachmentId: Int) : TaskDetailCommand
    data class InputCommentQuery(val query: String) : TaskDetailCommand
    data object AddComment : TaskDetailCommand
    data class StartEditComment(val commentId: Int) : TaskDetailCommand
    data class EditCommentTextChanged(val text: String) : TaskDetailCommand
    data object FinishEditComment : TaskDetailCommand
    data object CancelEditComment : TaskDetailCommand
    data class DeleteComment(val commentId: Int) : TaskDetailCommand
    data object Retry : TaskDetailCommand
}

sealed interface TaskDetailUiState {
    data object Initial : TaskDetailUiState
    data object Loading : TaskDetailUiState
    data class Content(
        val user: User,
        val task: TaskUi,
        val attachments: List<AttachmentUiModel>,
        val comments: CommentBlock
    ) : TaskDetailUiState

    data class Error(
        val message: String
    ) : TaskDetailUiState
}