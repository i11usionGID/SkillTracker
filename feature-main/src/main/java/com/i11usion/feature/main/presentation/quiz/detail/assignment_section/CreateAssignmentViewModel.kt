package com.i11usion.feature.main.presentation.quiz.detail.assignment_section

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.i11usion.core.domain.usecase.quiz.CreateAssignmentUseCase
import com.i11usion.core.domain.usecase.task.GetTaskListUseCase
import com.i11usion.core.domain.usecase.user.GetUsersListUseCase
import kotlinx.coroutines.async
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
    private val createAssignmentUseCase: CreateAssignmentUseCase
) : ViewModel() {

    private var currentQuizId: Int? = null

    private val _state =
        MutableStateFlow<CreateAssignmentSectionUiState>(CreateAssignmentSectionUiState.Initial)
    val state = _state.asStateFlow()

    private val _events = MutableSharedFlow<CreateAssignmentSectionEvent>(extraBufferCapacity = 1)
    val events = _events.asSharedFlow()

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
                } // ✅ либо/либо

            is CreateAssignmentSectionCommand.SelectTask ->
                updateContent {
                    it.copy(
                        selectedTaskId = command.taskId,
                        selectedUserId = null
                    )
                } // ✅ либо/либо

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
        }
    }

    private fun init(quizId: Int) {
        currentQuizId = quizId
        _state.value = CreateAssignmentSectionUiState.Loading

        viewModelScope.launch {
            runCatching {
                val usersDeferred = async { getUsersListUseCase(role = "employee") }
                val tasksDeferred = async { getTaskListUseCase() }

                val users = usersDeferred.await()
                val tasks = tasksDeferred.await()

                CreateAssignmentSectionUiState.Content(
                    employees = users.map { u ->
                        EmployeeOption(
                            id = u.id,
                            label = u.username
                        )
                    },
                    tasks = tasks.map { t ->
                        TaskOption(
                            id = t.id,
                            label = t.title
                        )
                    },

                    selectedUserId = null,
                    selectedTaskId = null,
                    dueDateMillis = null,
                    maxAttempts = null,
                    cooldownHours = 0,
                    required = false,
                    isCreating = false
                )
            }.onSuccess { content ->
                _state.value = content
            }.onFailure { t ->
                _state.value = CreateAssignmentSectionUiState.Error(
                    t.message ?: "Не удалось загрузить пользователей/задачи"
                )
            }
        }
    }

    private fun create() {
        val quizId = currentQuizId ?: return
        val s = _state.value as? CreateAssignmentSectionUiState.Content ?: return
        if (s.isCreating) return

        val userOrTaskOk = (s.selectedUserId != null) || (s.selectedTaskId != null)
        if (!userOrTaskOk) {
            _events.tryEmit(CreateAssignmentSectionEvent.ShowError("Нужно выбрать пользователя или привязать к задаче"))
            return
        }

        _state.value = s.copy(isCreating = true)

        viewModelScope.launch {
            runCatching {
                createAssignmentUseCase(
                    quizId = quizId,
                    assignedToUserId = s.selectedUserId,
                    taskId = s.selectedTaskId,
                    required = s.required,
                    deadline = s.dueDateMillis?.toDueAtLocalStartOfDay(),
                    maxAttempts = s.maxAttempts,
                    cooldownHours = s.cooldownHours
                )
            }.onSuccess {
                _events.tryEmit(CreateAssignmentSectionEvent.Created)
            }.onFailure { t ->
                val cur = _state.value as? CreateAssignmentSectionUiState.Content
                if (cur != null) _state.value = cur.copy(isCreating = false)
                _events.tryEmit(
                    CreateAssignmentSectionEvent.ShowError(
                        t.message ?: "Не удалось создать назначение"
                    )
                )
            }
        }
    }

    private inline fun updateContent(block: (CreateAssignmentSectionUiState.Content) -> CreateAssignmentSectionUiState.Content) {
        val s = _state.value as? CreateAssignmentSectionUiState.Content ?: return
        if (s.isCreating) return
        _state.value = block(s).normalize()
    }

    private fun CreateAssignmentSectionUiState.Content.normalize(): CreateAssignmentSectionUiState.Content {
        val fixedMaxAttempts = maxAttempts?.coerceIn(1, 10)
        val fixedCooldown = cooldownHours.coerceIn(0, 24)
        return copy(maxAttempts = fixedMaxAttempts, cooldownHours = fixedCooldown)
    }
}

/**
 * Сервер ждёт due_at: string|null.
 * Мы превращаем выбранную дату (millis) в ISO-строку UTC.
 * Если бэк ждёт локальную зону/время — подстрой здесь.
 */
private fun Long.toDueAtLocalStartOfDay(): Instant {
    val zone = java.time.ZoneId.systemDefault()
    val date = java.time.Instant.ofEpochMilli(this)
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
}

sealed interface CreateAssignmentSectionEvent {
    data object Close : CreateAssignmentSectionEvent
    data object Created : CreateAssignmentSectionEvent
    data class ShowError(val message: String) : CreateAssignmentSectionEvent
}

sealed interface CreateAssignmentSectionUiState {
    data object Initial : CreateAssignmentSectionUiState
    data object Loading : CreateAssignmentSectionUiState
    data class Error(val message: String) : CreateAssignmentSectionUiState

    data class Content(
        val employees: List<EmployeeOption>,
        val tasks: List<TaskOption>,

        val selectedUserId: Int?,
        val selectedTaskId: Int?,

        val dueDateMillis: Long?,
        val maxAttempts: Int?,
        val cooldownHours: Int,
        val required: Boolean,

        val isCreating: Boolean
    ) : CreateAssignmentSectionUiState {
        val canCreate: Boolean
            get() = (selectedUserId != null || selectedTaskId != null) && !isCreating
    }
}

data class EmployeeOption(val id: Int, val label: String)
data class TaskOption(val id: Int, val label: String)

