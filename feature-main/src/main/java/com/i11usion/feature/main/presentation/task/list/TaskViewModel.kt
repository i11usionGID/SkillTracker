package com.i11usion.feature.main.presentation.task.list

import AppError
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.i11usion.core.domain.model.network.AppResult
import com.i11usion.core.domain.model.task.TaskStatus
import com.i11usion.core.domain.model.user.Role
import com.i11usion.core.domain.model.user.User
import com.i11usion.core.domain.usecase.session.GetUserInfoUseCase
import com.i11usion.core.domain.usecase.task.GetTaskListUseCase
import com.i11usion.core.domain.usecase.user.GetUsersListUseCase
import com.i11usion.core.ui.formatter.formatInstantToStringDataNullable
import com.i11usion.core.ui.model.ErrorScreenModel
import com.i11usion.core.ui.model.UiError
import com.i11usion.core.ui.model.UiText
import com.i11usion.feature.main.manager.SnackbarManager
import com.i11usion.feature.main.mapper.toScreenUiError
import com.i11usion.feature.main.presentation.dashboard.mapper.taskToTaskUi
import com.i11usion.feature.main.presentation.model.TaskUi
import com.i11usion.feature.main.presentation.task.list.model.TaskFilters
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

class TaskViewModel @Inject constructor(
    private val getUserInfoUseCase: GetUserInfoUseCase,
    private val getTaskListUseCase: GetTaskListUseCase,
    private val getUsersListUseCase: GetUsersListUseCase,
    private val snackbarManager: SnackbarManager
) : ViewModel() {

    private companion object {
        const val RETRY_DELAY_MS = 10_000L
    }

    private val filters = MutableStateFlow(TaskFilters())

    private val reloadTasks = MutableSharedFlow<Unit>(
        extraBufferCapacity = 1
    )

    private val _state = MutableStateFlow(TaskUiState(isScreenLoading = true))
    val state = _state.asStateFlow()

    private var isFirstLoading: Boolean = true

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

            TaskCommand.RetryLoadTasks -> onRetry()
            TaskCommand.Refresh -> {
                if (isFirstLoading) {
                    isFirstLoading = false
                    return
                } else {
                    reloadTasks.tryEmit(Unit)
                }
            }
        }
    }

    private fun onRetry() {
        val hasFsError = _state.value.fullScreenError != null || _state.value.user == null
        if (hasFsError) loadInitial() else reloadTasks.tryEmit(Unit)
    }

    private fun loadInitial() {
        viewModelScope.launch {
            _state.update {
                it.copy(
                    isScreenLoading = true,
                    fullScreenError = null
                )
            }

            val user = getUserInfoUseCase()
            if (user == null) {
                _state.update {
                    it.copy(
                        isScreenLoading = false,
                        fullScreenError = ErrorScreenModel(
                            iconResId = com.i11usion.core.ui.R.drawable.ic_fail,
                            cdResId = com.i11usion.core.ui.R.string.cd_unknown_error,
                            text = UiText.Dynamic("Не удалось получить пользователя")
                        )
                    )
                }
                return@launch
            }

            if (user.role != Role.MANAGER) {
                _state.update {
                    it.copy(
                        user = user,
                        employees = emptyList(),
                        isScreenLoading = false,
                        fullScreenError = null
                    )
                }
                reloadTasks.tryEmit(Unit)
                return@launch
            }

            when (
                val result = retryRequest {
                    getUsersListUseCase(
                        page = null,
                        perPage = null,
                        search = null,
                        role = "employee"
                    )
                }
            ) {
                is AppResult.Success -> {
                    val employees = result.data

                    _state.update {
                        it.copy(
                            user = user,
                            employees = employees,
                            isScreenLoading = false,
                            fullScreenError = null
                        )
                    }
                    reloadTasks.tryEmit(Unit)
                }

                is AppResult.Failure -> {
                    showUiError(result.error)
                }
            }
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
            .onEach { currentFilters ->
                _state.update { st ->
                    st.copy(
                        taskFilters = currentFilters,
                        isListLoading = true,
                        fullScreenError = null
                    )
                }
            }
            .flatMapLatest { currentFilters ->
                flow {
                    emit(
                        retryRequest {
                            getTaskListUseCase(
                                status = currentFilters.status,
                                employeeId = currentFilters.employeeId,
                                search = currentFilters.query
                            )
                        }
                    )
                }
            }
            .onEach { result ->
                when (result) {
                    is AppResult.Success<*> -> {
                        @Suppress("UNCHECKED_CAST")
                        val domainTasks =
                            result.data as List<com.i11usion.core.domain.model.task.Task>

                        val tasks = domainTasks.map {
                            it.taskToTaskUi { date -> formatInstantToStringDataNullable(date) }
                        }

                        _state.update { st ->
                            st.copy(
                                taskList = tasks,
                                isListLoading = false,
                                fullScreenError = null
                            )
                        }
                    }

                    is AppResult.Failure -> {
                        _state.update { st -> st.copy(isListLoading = false) }
                        showUiError(result.error)
                    }
                }
            }
            .launchIn(viewModelScope)
    }

    private suspend fun <T> retryRequest(
        block: suspend () -> AppResult<T>
    ): AppResult<T> {
        while (true) {
            when (val result = block()) {
                is AppResult.Failure -> {
                    if (!shouldRetry(result.error)) return result
                    delay(RETRY_DELAY_MS)
                }

                is AppResult.Success<*> -> {
                    @Suppress("UNCHECKED_CAST")
                    return result as AppResult<T>
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

    private fun showUiError(error: AppError) {
        when (val uiError = error.toScreenUiError()) {
            is UiError.SnackBar -> snackbarManager.show(uiError.message)

            is UiError.FullScreen -> {
                _state.value = _state.value.copy(fullScreenError = uiError.model)
            }
        }
    }
}


sealed interface TaskCommand {

    data class InputSearchQuery(val query: String) : TaskCommand
    data class SelectStatus(val status: TaskStatus?) : TaskCommand
    data class SelectEmployee(val employeeId: Int?) : TaskCommand
    data object RetryLoadTasks : TaskCommand
    data object Refresh : TaskCommand
}

data class TaskUiState(
    val user: User? = null,
    val taskFilters: TaskFilters = TaskFilters(),
    val taskList: List<TaskUi> = emptyList(),
    val employees: List<User> = emptyList(),
    val isScreenLoading: Boolean = false,
    val isListLoading: Boolean = false,
    val fullScreenError: ErrorScreenModel? = null
)