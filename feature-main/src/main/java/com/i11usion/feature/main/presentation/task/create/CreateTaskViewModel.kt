package com.i11usion.feature.main.presentation.task.create

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.i11usion.core.domain.model.task.TaskStatus
import com.i11usion.core.domain.model.user.User
import com.i11usion.core.domain.usecase.task.CreateTaskUseCase
import com.i11usion.core.domain.usecase.user.GetUsersListUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.Instant
import javax.inject.Inject

class CreateTaskViewModel @Inject constructor(
    private val getUsersListUseCase: GetUsersListUseCase,
    private val createTaskUseCase: CreateTaskUseCase
) : ViewModel() {

    private val _state = MutableStateFlow<CreateTaskUiState>(CreateTaskUiState.Loading)
    val state = _state.asStateFlow()

    init {
        loadEmployees()
    }

    private fun loadEmployees() {
        _state.value = CreateTaskUiState.Loading

        viewModelScope.launch {
            try {
                val users = getUsersListUseCase(
                    page = null,
                    perPage = null,
                    search = null,
                    role = "employee"
                )

                _state.value = CreateTaskUiState.Content(
                    employeeList = users,
                    selectedStatus = TaskStatus.PENDING
                ).revalidate()
            } catch (t: Throwable) {
                _state.value = CreateTaskUiState.Error(
                    message = t.message ?: "Не удалось загрузить список сотрудников"
                )
            }
        }
    }

    fun processCommand(command: CreateTaskCommand) {
        val current = _state.value
        if (current !is CreateTaskUiState.Content) return

        when (command) {
            is CreateTaskCommand.ChangeTaskNameQuery -> {
                _state.value = current.copy(
                    taskNameQuery = command.query,
                    errorMessage = null
                ).revalidate()
            }

            is CreateTaskCommand.ChangeTaskDescriptionQuery -> {
                _state.value = current.copy(
                    taskDescriptionQuery = command.query,
                    errorMessage = null
                ).revalidate()
            }

            is CreateTaskCommand.ChangeEmployee -> {
                _state.value = current.copy(
                    selectedEmployeeId = command.userId,
                    errorMessage = null
                ).revalidate()
            }

            is CreateTaskCommand.ChangeDeadline -> {
                _state.value = current.copy(
                    deadlineMillis = command.dateMillis,
                    errorMessage = null
                ).revalidate()
            }

            is CreateTaskCommand.ChangeStatus -> {

                val newProgress = when (command.status) {
                    TaskStatus.IN_PROGRESS -> if (current.progress == 0) 1 else current.progress
                    TaskStatus.DONE -> 100
                    else -> 0
                }

                _state.value = current.copy(
                    selectedStatus = command.status,
                    progress = newProgress,
                    errorMessage = null
                ).revalidate()
            }

            is CreateTaskCommand.ChangeProgress -> {
                val newProgress = command.progress.coerceIn(0, 100)

                val newStatus = when (newProgress) {
                    0 -> TaskStatus.PENDING
                    in 1..99 -> TaskStatus.IN_PROGRESS
                    100 -> TaskStatus.DONE
                    else -> current.selectedStatus
                }

                _state.value = current.copy(
                    progress = newProgress,
                    selectedStatus = newStatus
                ).revalidate()
            }


            CreateTaskCommand.CreateTask -> {
                createTask(current)
            }
        }
    }

    private fun createTask(current: CreateTaskUiState.Content) {
        if (!current.canCreate || current.isCreating) return

        _state.value = current.copy(isCreating = true, errorMessage = null)

        viewModelScope.launch {
            try {
                val employeeId = current.selectedEmployeeId
                    ?: error("Исполнитель задачи не выбран")

                val title = current.taskNameQuery.trim()
                val description = current.taskDescriptionQuery.trim().ifBlank { null }

                val deadline: Instant? = current.deadlineMillis?.let { millis ->
                    Instant.ofEpochMilli(millis)
                }

                createTaskUseCase(
                    title = title,
                    description = description,
                    deadline = deadline,
                    status = current.selectedStatus,
                    progress = current.progress,
                    employeeId = employeeId
                )

                _state.value = current.copy(
                    isCreating = false,
                    errorMessage = null
                ).revalidate()

            } catch (t: Throwable) {
                val latest = _state.value
                if (latest is CreateTaskUiState.Content) {
                    _state.value = latest.copy(
                        isCreating = false,
                        errorMessage = t.message ?: "Не удалось создать задачу"
                    ).revalidate()
                } else {
                    _state.value = CreateTaskUiState.Error(
                        message = t.message ?: "Не удалось создать задачу"
                    )
                }
            }
        }
    }

    private fun CreateTaskUiState.Content.revalidate(): CreateTaskUiState.Content {
        val can = taskNameQuery.isNotBlank()
                && selectedEmployeeId != null
                && progress in 0..100

        return copy(canCreate = can)
    }
}

sealed interface CreateTaskCommand {
    data class ChangeTaskNameQuery(val query: String) : CreateTaskCommand
    data class ChangeTaskDescriptionQuery(val query: String) : CreateTaskCommand
    data class ChangeEmployee(val userId: Int?) : CreateTaskCommand
    data class ChangeDeadline(val dateMillis: Long?) : CreateTaskCommand
    data class ChangeStatus(val status: TaskStatus) : CreateTaskCommand
    data class ChangeProgress(val progress: Int) : CreateTaskCommand
    data object CreateTask : CreateTaskCommand
}


sealed interface CreateTaskUiState {
    data object Initial : CreateTaskUiState
    data object Loading : CreateTaskUiState
    data class Content(
        val taskNameQuery: String = "",
        val taskDescriptionQuery: String = "",
        val employeeList: List<User> = emptyList(),
        val selectedEmployeeId: Int? = null,
        val deadlineMillis: Long? = null,
        val selectedStatus: TaskStatus = TaskStatus.PENDING,
        val progress: Int = 0,
        val isCreating: Boolean = false,
        val canCreate: Boolean = false,
        val errorMessage: String? = null
    ) : CreateTaskUiState


    data class Error(
        val message: String
    ) : CreateTaskUiState
}