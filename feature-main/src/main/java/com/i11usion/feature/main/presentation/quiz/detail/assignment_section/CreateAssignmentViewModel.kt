package com.i11usion.feature.main.presentation.quiz.detail.assignment_section

import AppError
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.i11usion.core.domain.helpers.zip
import com.i11usion.core.domain.model.network.AppResult
import com.i11usion.core.domain.usecase.quiz.CreateAssignmentUseCase
import com.i11usion.core.domain.usecase.task.GetTaskListUseCase
import com.i11usion.core.domain.usecase.user.GetUsersListUseCase
import com.i11usion.core.ui.model.ErrorScreenModel
import com.i11usion.core.ui.model.UiError
import com.i11usion.feature.main.manager.SnackbarManager
import com.i11usion.feature.main.mapper.toScreenUiError
import com.i11usion.feature.main.presentation.quiz.detail.assignment_section.CreateAssignmentSectionUiState.Content
import com.i11usion.feature.main.presentation.quiz.detail.assignment_section.CreateAssignmentSectionUiState.Error
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.Instant
import javax.inject.Inject

class CreateAssignmentViewModel @Inject constructor(
    private val getUsersListUseCase: GetUsersListUseCase,
    private val getTaskListUseCase: GetTaskListUseCase,
    private val createAssignmentUseCase: CreateAssignmentUseCase,
    private val snackbarManager: SnackbarManager
) : ViewModel() {

    private var currentQuizId: Int? = null

    private val _state =
        MutableStateFlow<CreateAssignmentSectionUiState>(CreateAssignmentSectionUiState.Initial)
    val state = _state.asStateFlow()

    private val _events = MutableSharedFlow<CreateAssignmentSectionEvent>(extraBufferCapacity = 1)
    val events = _events.asSharedFlow()

    private var initJob: Job? = null
    private var retryJob: Job? = null

    fun processCommand(command: CreateAssignmentSectionCommand) {
        when (command) {
            is CreateAssignmentSectionCommand.Init -> init(command.quizId)

            CreateAssignmentSectionCommand.CloseClicked ->
                _events.tryEmit(CreateAssignmentSectionEvent.Close)

            is CreateAssignmentSectionCommand.SelectUser ->
                updateContent {
                    it.copy(
                        selectedUserId = command.userId,
                        selectedTaskId = null
                    )
                }

            is CreateAssignmentSectionCommand.SelectTask ->
                updateContent {
                    it.copy(
                        selectedTaskId = command.taskId,
                        selectedUserId = null
                    )
                }

            CreateAssignmentSectionCommand.ClearUser ->
                updateContent { it.copy(selectedUserId = null) }

            CreateAssignmentSectionCommand.ClearTask ->
                updateContent { it.copy(selectedTaskId = null) }

            is CreateAssignmentSectionCommand.SetDueDateMillis ->
                updateContent { it.copy(dueDateMillis = command.value) }

            CreateAssignmentSectionCommand.ResetDueDate ->
                updateContent { it.copy(dueDateMillis = null) }

            is CreateAssignmentSectionCommand.ChangeMaxAttempts ->
                updateContent { it.copy(maxAttempts = command.value?.coerceIn(1, 10)) }

            CreateAssignmentSectionCommand.ResetMaxAttempts ->
                updateContent { it.copy(maxAttempts = null) }

            is CreateAssignmentSectionCommand.ChangeCooldownHours ->
                updateContent { it.copy(cooldownHours = command.value.coerceIn(0, 24)) }

            CreateAssignmentSectionCommand.ToggleRequired ->
                updateContent { it.copy(required = !it.required) }

            CreateAssignmentSectionCommand.CreateClicked ->
                create()

            CreateAssignmentSectionCommand.Refresh -> onErrorRefreshClick()
        }
    }

    private fun onErrorRefreshClick() {
        when (_state.value) {
            is Content -> _state.value =
                (_state.value as Content).copy(fullScreenError = null)

            else -> {
                val quizId = currentQuizId ?: return
                init(quizId)
            }
        }
    }

    private fun init(quizId: Int) {
        currentQuizId = quizId
        _state.value = CreateAssignmentSectionUiState.Loading

        retryJob?.cancel()
        retryJob = null

        initJob?.cancel()
        initJob = viewModelScope.launch {
            val usersDeferred = async { getUsersListUseCase(role = "employee") }
            val tasksDeferred = async { getTaskListUseCase() }

            val result = usersDeferred.await()
                .zip(tasksDeferred.await()) { users, tasks -> users to tasks }

            when (result) {
                is AppResult.Success -> {
                    val (users, tasks) = result.data
                    _state.value = Content(
                        employees = users.map {
                            EmployeeOption(
                                it.id,
                                "${it.name} @${it.username}"
                            )
                        },
                        tasks = tasks.map { TaskOption(it.id, it.title) },
                        selectedUserId = null,
                        selectedTaskId = null,
                        dueDateMillis = null,
                        maxAttempts = null,
                        cooldownHours = 0,
                        required = false,
                        isCreating = false
                    )
                }

                is AppResult.Failure -> showUiError(result.error)
            }
        }
    }

    private fun create() {
        val quizId = currentQuizId ?: return
        val s = _state.value as? Content ?: return
        if (s.isCreating) return

        if (s.dueDateMillis == null) {
            showUiError(AppError.Validation(message = "Срок выполнения не указан"))
            return
        }

        _state.value = s.copy(isCreating = true)

        viewModelScope.launch {
            when (val result = createAssignmentUseCase(
                quizId = quizId,
                assignedToUserId = s.selectedUserId,
                taskId = s.selectedTaskId,
                required = s.required,
                deadline = s.dueDateMillis.toDueAtLocalStartOfDay(),
                maxAttempts = s.maxAttempts,
                cooldownHours = s.cooldownHours
            )) {
                is AppResult.Failure -> {
                    _state.value = s.copy(isCreating = false)
                    showUiError(result.error)
                }

                is AppResult.Success -> {
                    _events.tryEmit(CreateAssignmentSectionEvent.Created)
                }
            }
        }
    }

    private inline fun updateContent(block: (Content) -> Content) {
        val s = _state.value as? Content ?: return
        if (s.isCreating) return
        _state.value = block(s).normalize()
    }

    private fun Content.normalize(): Content {
        val fixedMaxAttempts = maxAttempts?.coerceIn(1, 10)
        val fixedCooldown = cooldownHours.coerceIn(0, 24)
        return copy(maxAttempts = fixedMaxAttempts, cooldownHours = fixedCooldown)
    }

    private fun showUiError(error: AppError) {
        when (val uiError = error.toScreenUiError()) {
            is UiError.SnackBar -> {
                snackbarManager.show(uiError.message)

                if (_state.value is CreateAssignmentSectionUiState.Loading) {
                    scheduleRetry()
                }
            }

            is UiError.FullScreen -> {
                val current = _state.value
                if (current is Content) {
                    _state.value = current.copy(fullScreenError = uiError.model)
                } else {
                    _state.value = Error(uiError.model)
                }
            }
        }
    }

    private fun scheduleRetry() {
        val quizId = currentQuizId ?: return

        retryJob?.cancel()
        retryJob = viewModelScope.launch {
            delay(10_000)

            val s = _state.value
            if (s is CreateAssignmentSectionUiState.Loading || s is Error) {
                init(quizId)
            }
        }
    }

    override fun onCleared() {
        initJob?.cancel()
        retryJob?.cancel()
        super.onCleared()
    }
}

private fun Long.toDueAtLocalStartOfDay(): Instant {
    val zone = java.time.ZoneId.systemDefault()
    val date = Instant.ofEpochMilli(this)
        .atZone(zone)
        .toLocalDate()

    return date
        .atStartOfDay(zone)
        .toInstant()
}


sealed interface CreateAssignmentSectionCommand {
    data class Init(val quizId: Int) : CreateAssignmentSectionCommand

    data object CloseClicked : CreateAssignmentSectionCommand

    data class SelectUser(val userId: Int?) : CreateAssignmentSectionCommand
    data class SelectTask(val taskId: Int?) : CreateAssignmentSectionCommand
    data object ClearUser : CreateAssignmentSectionCommand
    data object ClearTask : CreateAssignmentSectionCommand

    data class SetDueDateMillis(val value: Long) : CreateAssignmentSectionCommand
    data object ResetDueDate : CreateAssignmentSectionCommand

    data class ChangeMaxAttempts(val value: Int?) : CreateAssignmentSectionCommand
    data object ResetMaxAttempts : CreateAssignmentSectionCommand

    data class ChangeCooldownHours(val value: Int) : CreateAssignmentSectionCommand

    data object ToggleRequired : CreateAssignmentSectionCommand
    data object CreateClicked : CreateAssignmentSectionCommand
    data object Refresh : CreateAssignmentSectionCommand
}

sealed interface CreateAssignmentSectionEvent {
    data object Close : CreateAssignmentSectionEvent
    data object Created : CreateAssignmentSectionEvent
}

sealed interface CreateAssignmentSectionUiState {
    data object Initial : CreateAssignmentSectionUiState
    data object Loading : CreateAssignmentSectionUiState

    data class Content(
        val employees: List<EmployeeOption>,
        val tasks: List<TaskOption>,

        val selectedUserId: Int?,
        val selectedTaskId: Int?,

        val dueDateMillis: Long?,
        val maxAttempts: Int?,
        val cooldownHours: Int,
        val required: Boolean,

        val isCreating: Boolean,
        val fullScreenError: ErrorScreenModel? = null
    ) : CreateAssignmentSectionUiState {
        val canCreate: Boolean
            get() = (selectedUserId != null || selectedTaskId != null) && !isCreating
    }

    data class Error(val model: ErrorScreenModel) : CreateAssignmentSectionUiState
}

data class EmployeeOption(val id: Int, val label: String)
data class TaskOption(val id: Int, val label: String)

