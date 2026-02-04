package com.i11usion.feature.main.presentation.task

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.i11usion.core.domain.model.Role
import com.i11usion.core.domain.model.TaskStatus
import com.i11usion.core.domain.model.User
import com.i11usion.core.domain.usecase.GetTaskListUseCase
import com.i11usion.core.domain.usecase.GetUserInfoUseCase
import com.i11usion.core.domain.usecase.GetUsersListUseCase
import com.i11usion.core.ui.formatter.formatInstantToStringDataNullable
import com.i11usion.feature.main.presentation.dashboard.mapper.taskToTaskUi
import com.i11usion.feature.main.presentation.model.TaskUi
import com.i11usion.feature.main.presentation.task.model.TaskFilters
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

class TaskViewModel @Inject constructor(
    private val getUserInfoUseCase: GetUserInfoUseCase,
    private val getTaskListUseCase: GetTaskListUseCase,
    private val getUsersListUseCase: GetUsersListUseCase
) : ViewModel() {

    private val filters = MutableStateFlow(TaskFilters())

    private val reloadTasks = MutableSharedFlow<Unit>(
        extraBufferCapacity = 1
    )

    private val _state = MutableStateFlow(TaskUiState(isScreenLoading = true))
    val state = _state.asStateFlow()

    init {
        observeFiltersAndLoadTasks()
        loadInitial()
    }

    fun processCommand(command: TaskCommand) {
        when (command) {
            is TaskCommand.InputSearchQuery -> {
                val raw = command.query

                _state.update { st ->
                    st.copy(taskFilters = st.taskFilters.copy(query = raw))
                }

                filters.update { it.copy(query = raw.trim()) }
            }

            is TaskCommand.SelectStatus -> {
                _state.update { st -> st.copy(taskFilters = st.taskFilters.copy(status = command.status)) }
                filters.update { it.copy(status = command.status) }
            }

            is TaskCommand.SelectEmployee -> {
                val isManager = _state.value.user?.role == Role.MANAGER
                if (isManager) {
                    _state.update { st -> st.copy(taskFilters = st.taskFilters.copy(employeeId = command.employeeId)) }
                    filters.update { it.copy(employeeId = command.employeeId) }
                }
            }

            TaskCommand.RetryLoadTasks -> reloadTasks.tryEmit(Unit)
        }
    }


    private fun loadInitial() {
        viewModelScope.launch {
            _state.update { it.copy(isScreenLoading = true, error = null) }

            runCatching {
                val user = getUserInfoUseCase()
                requireNotNull(user) { "Не удалось получить пользователя" }

                val employees = if (user.role == Role.MANAGER) {
                    getUsersListUseCase(
                        page = null,
                        perPage = null,
                        search = null,
                        role = "employee"
                    )
                } else {
                    emptyList()
                }

                user to employees
            }.fold(
                onSuccess = { (user, employees) ->
                    _state.update {
                        it.copy(
                            user = user,
                            employees = employees,
                            isScreenLoading = false,
                            error = null
                        )
                    }
                    reloadTasks.tryEmit(Unit)
                },
                onFailure = { t ->
                    _state.update {
                        it.copy(
                            isScreenLoading = false,
                            error = t.message ?: "Не удалось загрузить данные"
                        )
                    }
                }
            )
        }
    }

    @OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
    private fun observeFiltersAndLoadTasks() {
        val queryFlow = filters
            .map { it.query }
            .distinctUntilChanged()
            .debounce(300)

        val statusFlow = filters.map { it.status }.distinctUntilChanged()
        val employeeFlow = filters.map { it.employeeId }.distinctUntilChanged()

        combine(queryFlow, statusFlow, employeeFlow) { q, s, e ->
            TaskFilters(query = q, status = s, employeeId = e)
        }
            .distinctUntilChanged()
            .let { filtersFlow ->
                merge(
                    filtersFlow,
                    reloadTasks.map { filters.value }
                )
            }
            .onEach {
                _state.update { st -> st.copy(isListLoading = true, error = null) }
            }
            .mapLatest { currentFilters ->
                val result = runCatching {
                    getTaskListUseCase(
                        status = currentFilters.status,
                        employeeId = currentFilters.employeeId,
                        search = currentFilters.query
                    ).map { it.taskToTaskUi { date -> formatInstantToStringDataNullable(date) } }
                }
                result
            }
            .onEach { result ->
                result.fold(
                    onSuccess = { tasks ->
                        _state.update { st ->
                            st.copy(taskList = tasks, isListLoading = false, error = null)
                        }
                    },
                    onFailure = { t ->
                        _state.update { st ->
                            st.copy(isListLoading = false, error = t.message ?: "Не удалось загрузить задачи")
                        }
                    }
                )
            }
            .launchIn(viewModelScope)
    }
}


sealed interface TaskCommand {

    data class InputSearchQuery(val query: String) : TaskCommand
    data class SelectStatus(val status: TaskStatus?) : TaskCommand
    data class SelectEmployee(val employeeId: Int?) : TaskCommand
    data object RetryLoadTasks : TaskCommand
}

data class TaskUiState(
    val user: User? = null,
    val taskFilters: TaskFilters = TaskFilters(),
    val taskList: List<TaskUi> = emptyList(),
    val employees: List<User> = emptyList(),
    val isScreenLoading: Boolean = false,
    val isListLoading: Boolean = false,
    val error: String? = null
)

