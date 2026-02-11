package com.i11usion.feature.main.presentation.user

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.i11usion.core.domain.model.user.Role
import com.i11usion.core.domain.usecase.user.DeleteUserUseCase
import com.i11usion.core.domain.usecase.user.GetUsersListUseCase
import com.i11usion.core.ui.formatter.formatInstantToStringData
import com.i11usion.feature.main.presentation.dashboard.mapper.userToUserUiModel
import com.i11usion.feature.main.presentation.user.model.UserFilters
import com.i11usion.feature.main.presentation.user.model.UserUiModel
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

class UserViewModel @Inject constructor(
    private val getUsersListUseCase: GetUsersListUseCase,
    private val deleteUserUseCase: DeleteUserUseCase
) : ViewModel() {

    private val filters = MutableStateFlow(UserFilters())
    private val reloadUsers = MutableSharedFlow<Unit>(
        extraBufferCapacity = 1
    )
    private val _state = MutableStateFlow<UserUiState>(UserUiState.Initial)
    val state = _state.asStateFlow()

    init {
        observeFiltersAndLoadTasks()
        loadInitial()
    }

    private fun loadInitial() {
        _state.value = UserUiState.Loading
        reloadUsers.tryEmit(Unit)
    }

    fun processCommand(command: UserCommand) {
        when (command) {
            is UserCommand.InputSearchQuery -> {
                val query = command.query
                filters.update { it.copy(query = query.trim()) }

                _state.update { state ->
                    when (state) {
                        is UserUiState.Content -> state.copy(
                            userFilters = state.userFilters.copy(query = query)
                        )
                        UserUiState.Loading, UserUiState.Initial ->
                            UserUiState.Content(userFilters = UserFilters(query = query), isListLoading = true)
                        is UserUiState.Error ->
                            UserUiState.Content(userFilters = state.userFilters.copy(query = query), isListLoading = true)
                    }
                }
            }

            is UserCommand.SelectRole -> {
                filters.update { it.copy(role = command.role) }
            }

            is UserCommand.DeleteUser -> {
                viewModelScope.launch {
                    deleteUserUseCase(command.userId)
                    reloadUsers.emit(Unit)
                }
            }

            UserCommand.RetryLoadUsers -> reloadUsers.tryEmit(Unit)
        }
    }

    @OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
    private fun observeFiltersAndLoadTasks() {
        val queryFlow = filters
            .map { it.query }
            .distinctUntilChanged()
            .debounce(300)

        val roleFlow = filters.map { it.role }.distinctUntilChanged()

        combine(queryFlow, roleFlow) { q, r ->
            UserFilters(query = q, role = r)
        }
            .distinctUntilChanged()
            .let { filtersFlow ->
                merge(
                    filtersFlow,
                    reloadUsers.map { filters.value }
                )
            }
            .onEach {
                _state.update { state ->
                    when (state) {
                        is UserUiState.Content -> state.copy(isListLoading = true)
                        else -> UserUiState.Content(userFilters = filters.value, isListLoading = true)
                    }
                }
            }
            .mapLatest { currentFilters ->
                val result = runCatching {
                    val role = when (currentFilters.role) {
                        Role.MANAGER -> "manager"
                        Role.EMPLOYEE -> "employee"
                        else -> null
                    }
                    getUsersListUseCase(
                        page = null,
                        perPage = null,
                        search = currentFilters.query,
                        role = role
                    ).map { it.userToUserUiModel { date -> formatInstantToStringData(date) } }
                }
                result
            }
            .onEach { result ->
                result.fold(
                    onSuccess = { users ->
                        _state.value = UserUiState.Content(
                            userFilters = filters.value,
                            userList = users,
                            isListLoading = false
                        )
                    },
                    onFailure = { t ->
                        _state.value = UserUiState.Error(
                            userFilters = filters.value,
                            message = t.message ?: "Не удалось загрузить задачи"
                        )
                    }
                )
            }
            .launchIn(viewModelScope)
    }
}

sealed interface UserCommand {
    data class InputSearchQuery(val query: String) : UserCommand
    data class SelectRole(val role: Role?) : UserCommand
    data object RetryLoadUsers : UserCommand
    data class DeleteUser(val userId: Int): UserCommand
}

sealed interface UserUiState {
    data object Initial : UserUiState
    data object Loading : UserUiState
    data class Content(
        val userFilters: UserFilters = UserFilters(),
        val userList: List<UserUiModel> = emptyList(),
        val isListLoading: Boolean = false
    ) : UserUiState

    data class Error(
        val userFilters: UserFilters = UserFilters(),
        val message: String = ""
    ) : UserUiState
}