package com.i11usion.feature.main.presentation.task.create

import AppError
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.i11usion.core.domain.model.network.AppResult
import com.i11usion.core.domain.model.task.TaskStatus
import com.i11usion.core.domain.model.user.User
import com.i11usion.core.domain.usecase.task.CreateTaskUseCase
import com.i11usion.core.domain.usecase.user.GetUsersListUseCase
import com.i11usion.core.ui.model.ErrorScreenModel
import com.i11usion.core.ui.model.UiError
import com.i11usion.feature.main.manager.SnackbarManager
import com.i11usion.feature.main.mapper.toScreenUiError
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant
import javax.inject.Inject

class CreateTaskViewModel @Inject constructor(
    private val getUsersListUseCase: GetUsersListUseCase,
    private val createTaskUseCase: CreateTaskUseCase,
    private val snackbarManager: SnackbarManager
) : ViewModel() {

    private val _state = MutableStateFlow<CreateTaskUiState>(CreateTaskUiState.Loading)
    val state = _state.asStateFlow()

    private val _events = MutableSharedFlow<CreateTaskEvents>()
    val events = _events.asSharedFlow()

    init {
        loadEmployees()
    }

    private fun loadEmployees() {
        _state.value = CreateTaskUiState.Loading

        viewModelScope.launch {
            when (val result =
                getUsersListUseCase(
                    page = null,
                    perPage = null,
                    search = null,
                    role = "employee"
                )
            ) {

                is AppResult.Success -> {
                    _state.value = CreateTaskUiState.Content(
                        employeeList = result.data,
                        selectedStatus = TaskStatus.PENDING
                    ).revalidate()
                }

                is AppResult.Failure -> {
                    showUiError(result.error)
                }
            }
        }
    }

    fun processCommand(command: CreateTaskCommand) {
        val current = _state.value
        if (current !is CreateTaskUiState.Content) return

        when (command) {
            is CreateTaskCommand.ChangeTaskNameQuery -> {
                _state.value = current.copy(
                    taskNameQuery = command.query
                ).revalidate()
            }

            is CreateTaskCommand.ChangeTaskDescriptionQuery -> {
                _state.value = current.copy(
                    taskDescriptionQuery = command.query
                ).revalidate()
            }

            is CreateTaskCommand.ChangeEmployee -> {
                _state.value = current.copy(
                    selectedEmployeeId = command.userId
                ).revalidate()
            }

            is CreateTaskCommand.ChangeDeadline -> {
                _state.value = current.copy(
                    deadlineMillis = command.dateMillis
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
                    progress = newProgress
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

            CreateTaskCommand.Refresh -> onErrorRefreshClick()
        }
    }

    private fun onErrorRefreshClick() {
        when (_state.value) {
            is CreateTaskUiState.Content -> _state.value =
                (_state.value as CreateTaskUiState.Content).copy(fullScreenError = null)

            else -> _state.value = CreateTaskUiState.Content()
        }
    }

    private fun createTask(current: CreateTaskUiState.Content) {
        if (!current.canCreate || current.isCreating) return

        _state.value = current.copy(isCreating = true)

        viewModelScope.launch {
            val employeeId = current.selectedEmployeeId
                ?: error("Исполнитель задачи не выбран")

            val title = current.taskNameQuery.trim()
            val description = current.taskDescriptionQuery.trim().ifBlank { null }

            val deadline: Instant? = current.deadlineMillis?.let { millis ->
                Instant.ofEpochMilli(millis)
            }

            when (val result = createTaskUseCase(
                title = title,
                description = description,
                deadline = deadline,
                status = current.selectedStatus,
                progress = current.progress,
                employeeId = employeeId
            )) {
                is AppResult.Success -> {
                    _events.emit(CreateTaskEvents.Created)
                }

                is AppResult.Failure -> {
                    _state.update { state ->
                        val base = current.copy(isCreating = false)
                        base.copy(canCreate = true)
                    }

                    showUiError(result.error)
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

    private fun showUiError(error: AppError) {
        when (val uiError = error.toScreenUiError()) {
            is UiError.SnackBar -> snackbarManager.show(uiError.message)

            is UiError.FullScreen -> {
                val current = _state.value
                if (current is CreateTaskUiState.Content) {
                    _state.value = current.copy(fullScreenError = uiError.model, isCreating = false)
                } else {
                    _state.value = CreateTaskUiState.Error(uiError.model)
                }
            }
        }
    }
}

sealed interface CreateTaskEvents {
    data object Created : CreateTaskEvents
}

sealed interface CreateTaskCommand {
    data class ChangeTaskNameQuery(val query: String) : CreateTaskCommand
    data class ChangeTaskDescriptionQuery(val query: String) : CreateTaskCommand
    data class ChangeEmployee(val userId: Int?) : CreateTaskCommand
    data class ChangeDeadline(val dateMillis: Long?) : CreateTaskCommand
    data class ChangeStatus(val status: TaskStatus) : CreateTaskCommand
    data class ChangeProgress(val progress: Int) : CreateTaskCommand
    data object CreateTask : CreateTaskCommand
    data object Refresh : CreateTaskCommand
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
        val fullScreenError: ErrorScreenModel? = null
    ) : CreateTaskUiState


    data class Error(
        val model: ErrorScreenModel
    ) : CreateTaskUiState
}