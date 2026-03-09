package com.i11usion.feature.main.presentation.task.detail

import AppError
import android.app.Application
import android.net.Uri
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.i11usion.core.domain.model.network.AppResult
import com.i11usion.core.domain.model.task.TaskStatus
import com.i11usion.core.domain.model.user.User
import com.i11usion.core.domain.usecase.session.GetUserInfoUseCase
import com.i11usion.core.domain.usecase.task.AddTaskCommentUseCase
import com.i11usion.core.domain.usecase.task.DeleteTaskAttachmentUseCase
import com.i11usion.core.domain.usecase.task.DeleteTaskCommentUseCase
import com.i11usion.core.domain.usecase.task.DeleteTaskUseCase
import com.i11usion.core.domain.usecase.task.DownloadTaskAttachmentUseCase
import com.i11usion.core.domain.usecase.task.GetTaskAttachmentsUseCase
import com.i11usion.core.domain.usecase.task.GetTaskByIdUseCase
import com.i11usion.core.domain.usecase.task.GetTaskCommentsUseCase
import com.i11usion.core.domain.usecase.task.UpdateTaskCommentUseCase
import com.i11usion.core.domain.usecase.task.UpdateTaskProgressUseCase
import com.i11usion.core.domain.usecase.task.UploadTaskAttachmentUseCase
import com.i11usion.core.ui.formatter.formatInstantToStringDataNullable
import com.i11usion.core.ui.model.ErrorScreenModel
import com.i11usion.core.ui.model.UiError
import com.i11usion.core.ui.model.UiText
import com.i11usion.feature.main.manager.SnackbarManager
import com.i11usion.feature.main.mapper.toScreenUiError
import com.i11usion.feature.main.presentation.dashboard.mapper.taskToTaskUi
import com.i11usion.feature.main.presentation.model.TaskUi
import com.i11usion.feature.main.presentation.task.detail.mapper.domainToUi
import com.i11usion.feature.main.presentation.task.detail.model.AttachmentUiModel
import com.i11usion.feature.main.presentation.task.detail.model.CommentBlock
import com.i11usion.feature.main.presentation.task.detail.model.EditableComment
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

class TaskDetailViewModel @Inject constructor(
    private val application: Application,
    private val getUserInfoUseCase: GetUserInfoUseCase,
    private val getTaskByIdUseCase: GetTaskByIdUseCase,
    private val deleteTaskUseCase: DeleteTaskUseCase,
    private val updateTaskProgressUseCase: UpdateTaskProgressUseCase,
    private val getTaskAttachmentsUseCase: GetTaskAttachmentsUseCase,
    private val uploadTaskAttachmentUseCase: UploadTaskAttachmentUseCase,
    private val downloadTaskAttachmentUseCase: DownloadTaskAttachmentUseCase,
    private val deleteTaskAttachmentUseCase: DeleteTaskAttachmentUseCase,
    private val getTaskCommentsUseCase: GetTaskCommentsUseCase,
    private val addTaskCommentUseCase: AddTaskCommentUseCase,
    private val updateTaskCommentUseCase: UpdateTaskCommentUseCase,
    private val deleteTaskCommentUseCase: DeleteTaskCommentUseCase,
    private val snackbarManager: SnackbarManager
) : ViewModel() {

    private companion object {
        const val RETRY_DELAY_MS = 10_000L
    }

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

    private val _events = MutableSharedFlow<TaskDetailEvent>()
    val events = _events.asSharedFlow()

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
            is TaskDetailCommand.DeleteAttachment -> deleteTaskAttachment(command.attachmentId)
            is TaskDetailCommand.DownloadAttachment -> downloadAttachment(command.attachmentId)

            is TaskDetailCommand.DeleteTask -> deleteTask(command.taskId)

            TaskDetailCommand.Retry -> retry()
            TaskDetailCommand.Refresh -> onErrorRefreshClick()
        }
    }

    private fun onErrorRefreshClick() {
        when (_state.value) {
            is TaskDetailUiState.Content -> _state.value =
                (_state.value as TaskDetailUiState.Content).copy(fullScreenError = null)

            else -> {
                val taskId = currentTaskId ?: return
                init(taskId)
            }
        }
    }

    private fun init(taskId: Int) {
        if (currentTaskId == taskId && _state.value is TaskDetailUiState.Content) return
        currentTaskId = taskId

        _state.value = TaskDetailUiState.Loading

        viewModelScope.launch {
            val u = getUserInfoUseCase() ?: return@launch
            user.value = u

            attachments.value = emptyList()
            commentsBlock.value = CommentBlock(
                query = commentsBlock.value.query,
                isLoading = false,
                commentList = emptyList(),
                editableComment = null
            )

            when (val result = retryRequest { getTaskByIdUseCase(taskId) }) {
                is AppResult.Success -> {
                    task.value = result.data.taskToTaskUi(
                        dateFormatter = { date ->
                            formatInstantToStringDataNullable(date)
                        }
                    )
                }

                is AppResult.Failure -> {
                    showUiError(result.error)
                    return@launch
                }
            }

            commentsBlock.update { it.copy(isLoading = true) }
            publishContentIfReady()

            when (val result = retryRequest { getTaskCommentsUseCase(taskId) }) {
                is AppResult.Success -> {
                    commentsBlock.value = commentsBlock.value.copy(
                        isLoading = false,
                        commentList = result.data.map { it.domainToUi() }
                    )
                    publishContentIfReady()
                }

                is AppResult.Failure -> {
                    commentsBlock.update { it.copy(isLoading = false) }
                    publishContentIfReady()
                    showUiError(result.error)
                    return@launch
                }
            }

            when (val result = retryRequest { getTaskAttachmentsUseCase(taskId) }) {
                is AppResult.Success -> {
                    attachments.value = result.data.map { it.domainToUi() }
                    publishContentIfReady()
                }

                is AppResult.Failure -> {
                    publishContentIfReady()
                    showUiError(result.error)
                }
            }
        }
    }

    private suspend fun <T> retryRequest(
        block: suspend () -> AppResult<T>
    ): AppResult<T> {
        while (true) {
            when (val result = block()) {
                is AppResult.Success -> return result
                is AppResult.Failure -> {
                    if (!shouldRetry(result.error)) return result
                    delay(RETRY_DELAY_MS)
                }
            }
        }
    }

    private fun shouldRetry(error: AppError): Boolean {
        return when (error) {
            is AppError.Network -> {
                snackbarManager.show(UiText.Dynamic("Отсутствует подключение к интернету"))
                true
            }
            else -> false
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
            val status = progressToStatus(newProgress)

            when (val result = updateTaskProgressUseCase(
                taskId = taskId,
                progress = newProgress,
                status = status
            )) {
                is AppResult.Success -> {
                    task.value = result.data.taskToTaskUi(
                        dateFormatter = { date ->
                            formatInstantToStringDataNullable(date)
                        }
                    )
                    publishContentIfReady()
                }

                is AppResult.Failure -> {
                    showUiError(result.error)
                }
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
            commentsBlock.update { it.copy(isLoading = true) }
            publishContentIfReady()

            when (val result = addTaskCommentUseCase(taskId = taskId, text = text)) {
                is AppResult.Success -> {
                    val newList = commentsBlock.value.commentList + result.data.domainToUi()

                    commentsBlock.value = commentsBlock.value.copy(
                        isLoading = false,
                        query = "",
                        commentList = newList
                    )
                    publishContentIfReady()
                }

                is AppResult.Failure -> {
                    commentsBlock.update { it.copy(isLoading = false) }
                    publishContentIfReady()
                    showUiError(result.error)
                }
            }
        }
    }

    private fun deleteTask(taskId: Int) {
        viewModelScope.launch {
            val currentState = _state.value
            _state.value = TaskDetailUiState.Loading
            when (val result = deleteTaskUseCase(taskId)) {
                is AppResult.Success -> {
                    _events.emit(TaskDetailEvent.NavigateBack)
                }

                is AppResult.Failure -> {
                    _state.value = currentState
                    showUiError(result.error)
                }
            }
        }
    }

    private fun deleteComment(commentId: Int) {
        val content = _state.value as? TaskDetailUiState.Content ?: return
        val taskId = content.task.id

        viewModelScope.launch {
            val old = commentsBlock.value
            when (val result = deleteTaskCommentUseCase(taskId = taskId, commentId = commentId)) {
                is AppResult.Success -> {
                    commentsBlock.value = old.copy(
                        commentList = old.commentList.filterNot { it.id == commentId },
                        editableComment = if (old.editableComment?.commentId == commentId) null else old.editableComment
                    )
                    publishContentIfReady()
                }

                is AppResult.Failure -> {
                    publishContentIfReady()
                    showUiError(result.error)
                }
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

            when (val result = updateTaskCommentUseCase(
                taskId = taskId,
                commentId = editable.commentId,
                text = newText
            )) {
                is AppResult.Success -> {
                    val updatedList = old.commentList.map {
                        if (it.id == editable.commentId) it.copy(text = newText) else it
                    }
                    commentsBlock.value = old.copy(commentList = updatedList, isLoading = true)

                    commentsBlock.value = commentsBlock.value.copy(
                        isLoading = false,
                        editableComment = null
                    )
                    publishContentIfReady()
                }

                is AppResult.Failure -> {
                    commentsBlock.value = old.copy(isLoading = false)
                    publishContentIfReady()
                    showUiError(result.error)
                }
            }
        }
    }

    private fun uploadAttachment(taskId: Int, uri: Uri) {
        viewModelScope.launch {
            when (val result = uploadTaskAttachmentUseCase(taskId = taskId, uri = uri)) {
                is AppResult.Success -> {
                    attachments.update { old ->
                        old + result.data.domainToUi()
                    }
                    publishContentIfReady()
                }

                is AppResult.Failure -> {
                    showUiError(result.error)
                }
            }
        }
    }

    private fun deleteTaskAttachment(attachmentId: Int) {
        val content = _state.value as? TaskDetailUiState.Content ?: return
        val taskId = content.task.id

        viewModelScope.launch {
            val old = attachments.value

            when (val result =
                deleteTaskAttachmentUseCase(taskId = taskId, attachmentId = attachmentId)) {
                is AppResult.Success -> {
                    attachments.value = old.filterNot { it.id == attachmentId }
                    publishContentIfReady()
                }

                is AppResult.Failure -> {
                    showUiError(result.error)
                }
            }
        }
    }

    private fun downloadAttachment(attachmentId: Int) {
        val content = _state.value as? TaskDetailUiState.Content ?: return
        val taskId = content.task.id

        viewModelScope.launch {
            when (val result =
                downloadTaskAttachmentUseCase(taskId = taskId, attachmentId = attachmentId)) {
                is AppResult.Success -> {
                    val uri = FileProvider.getUriForFile(
                        application,
                        "${application.packageName}.fileprovider",
                        result.data.file
                    )

                    _events.emit(TaskDetailEvent.OpenDownloadedFile(uri, result.data.mimeType))
                }

                is AppResult.Failure -> {
                    showUiError(result.error)
                }
            }
        }
    }

    private fun showUiError(error: AppError) {
        when (val uiError = error.toScreenUiError()) {
            is UiError.SnackBar -> snackbarManager.show(uiError.message)

            is UiError.FullScreen -> {
                val current = _state.value
                if (current is TaskDetailUiState.Content) {
                    _state.value = current.copy(fullScreenError = uiError.model)
                } else {
                    _state.value = TaskDetailUiState.Error(uiError.model)
                }
            }
        }
    }
}

sealed interface TaskDetailEvent {
    data object NavigateBack : TaskDetailEvent
    data class OpenDownloadedFile(val uri: Uri, val mimeType: String?) : TaskDetailEvent
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
    data object Refresh : TaskDetailCommand
}

sealed interface TaskDetailUiState {
    data object Initial : TaskDetailUiState
    data object Loading : TaskDetailUiState
    data class Content(
        val user: User,
        val task: TaskUi,
        val attachments: List<AttachmentUiModel>,
        val comments: CommentBlock,
        val fullScreenError: ErrorScreenModel? = null
    ) : TaskDetailUiState

    data class Error(
        val model: ErrorScreenModel
    ) : TaskDetailUiState
}