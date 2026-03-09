package com.i11usion.feature.main.presentation.user.list

import AppError
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.i11usion.core.domain.model.network.AppResult
import com.i11usion.core.domain.model.user.Role
import com.i11usion.core.domain.usecase.user.DeleteUserUseCase
import com.i11usion.core.domain.usecase.user.GetUsersListUseCase
import com.i11usion.core.ui.formatter.formatInstantToStringData
import com.i11usion.core.ui.model.ErrorScreenModel
import com.i11usion.core.ui.model.UiError
import com.i11usion.core.ui.model.UiText
import com.i11usion.feature.main.manager.SnackbarManager
import com.i11usion.feature.main.mapper.toScreenUiError
import com.i11usion.feature.main.presentation.dashboard.mapper.userToUserUiModel
import com.i11usion.feature.main.presentation.user.list.UserUiState.Content
import com.i11usion.feature.main.presentation.user.list.UserUiState.Error
import com.i11usion.feature.main.presentation.user.list.UserUiState.Initial
import com.i11usion.feature.main.presentation.user.list.UserUiState.Loading
import com.i11usion.feature.main.presentation.user.list.model.UserFilters
import com.i11usion.feature.main.presentation.user.list.model.UserUiModel
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

class UserViewModel @Inject constructor(
    private val getUsersListUseCase: GetUsersListUseCase,
    private val deleteUserUseCase: DeleteUserUseCase,
    private val snackbarManager: SnackbarManager
) : ViewModel() {

    private companion object {
        const val RETRY_DELAY_MS = 10_000L
    }

    private val filters = MutableStateFlow(UserFilters())
    private val reloadUsers = MutableSharedFlow<Unit>(
        extraBufferCapacity = 1
    )
    private val _state = MutableStateFlow<UserUiState>(Initial)
    val state = _state.asStateFlow()

    init {
        _state.value = Loading
        observeFiltersAndLoadUsers()
    }

    fun processCommand(command: UserCommand) {
        when (command) {
            is UserCommand.InputSearchQuery -> {
                val query = command.query
                filters.update { it.copy(query = query.trim()) }

                _state.update { state ->
                    when (state) {
                        is Content -> state.copy(
                            userFilters = state.userFilters.copy(query = query)
                        )

                        Loading, Initial ->
                            Content(userFilters = UserFilters(query = query), isListLoading = true)

                        is Error ->
                            Content(
                                userFilters = UserFilters(query = query),
                                isListLoading = true
                            )
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
            UserCommand.Refresh -> reloadUsers.tryEmit(Unit)
        }
    }

    @OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
    private fun observeFiltersAndLoadUsers() {
        val queryFlow = filters
            .map { it.query }
            .distinctUntilChanged()
            .debounce(300)

        val roleFlow = filters
            .map { it.role }
            .distinctUntilChanged()

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
            .onEach { currentFilters ->
                _state.update { state ->
                    when (state) {
                        is Content -> state.copy(
                            userFilters = currentFilters,
                            isListLoading = true,
                            fullScreenError = null
                        )

                        else -> Content(
                            userFilters = currentFilters,
                            isListLoading = true
                        )
                    }
                }
            }
            .flatMapLatest { currentFilters ->
                loadUsersUntilSuccess(currentFilters)
            }
            .onEach { result ->
                when (result) {
                    is AppResult.Success -> {
                        val users = result.data.map { u ->
                            u.userToUserUiModel { date -> formatInstantToStringData(date) }
                        }

                        _state.value = Content(
                            userFilters = filters.value,
                            userList = users,
                            isListLoading = false
                        )
                    }

                    is AppResult.Failure -> {
                        _state.update { state ->
                            when (state) {
                                is Content -> state.copy(isListLoading = false)
                                else -> state
                            }
                        }
                        showUiError(result.error)
                    }
                }
            }
            .launchIn(viewModelScope)
    }

    private fun loadUsersUntilSuccess(currentFilters: UserFilters) = flow {
        while (true) {
            val role = when (currentFilters.role) {
                Role.MANAGER -> "manager"
                Role.EMPLOYEE -> "employee"
                else -> null
            }

            when (
                val result = getUsersListUseCase(
                    page = null,
                    perPage = null,
                    search = currentFilters.query,
                    role = role
                )
            ) {
                is AppResult.Success -> {
                    emit(result)
                    break
                }

                is AppResult.Failure -> {
                    if (!shouldRetry(result.error)) {
                        emit(result)
                        break
                    }

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

    private fun showUiError(error: AppError) {
        when (val uiError = error.toScreenUiError()) {
            is UiError.SnackBar -> snackbarManager.show(uiError.message)

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
}

sealed interface UserCommand {
    data class InputSearchQuery(val query: String) : UserCommand
    data class SelectRole(val role: Role?) : UserCommand
    data object RetryLoadUsers : UserCommand
    data class DeleteUser(val userId: Int) : UserCommand
    data object Refresh : UserCommand
}

sealed interface UserUiState {
    data object Initial : UserUiState
    data object Loading : UserUiState
    data class Content(
        val userFilters: UserFilters = UserFilters(),
        val userList: List<UserUiModel> = emptyList(),
        val isListLoading: Boolean = false,
        val fullScreenError: ErrorScreenModel? = null
    ) : UserUiState

    data class Error(
        val model: ErrorScreenModel
    ) : UserUiState
}