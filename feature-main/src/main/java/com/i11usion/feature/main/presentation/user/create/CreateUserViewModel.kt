package com.i11usion.feature.main.presentation.user.create

import AppError
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.i11usion.core.domain.model.network.AppResult
import com.i11usion.core.domain.model.user.Role
import com.i11usion.core.domain.usecase.user.CreateUserUseCase
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
import javax.inject.Inject

class CreateUserViewModel @Inject constructor(
    private val createUserUseCase: CreateUserUseCase,
    private val snackbarManager: SnackbarManager
) : ViewModel() {

    private val _state = MutableStateFlow<CreateUserUiState>(CreateUserUiState.Content())
    val state = _state.asStateFlow()

    private val _events = MutableSharedFlow<CreateUserEvent>()
    val events = _events.asSharedFlow()

    fun processCommand(command: CreateUserCommand) {
        when (command) {
            is CreateUserCommand.ChangeNameQuery -> updateContent { it.copy(nameQuery = command.query) }
            is CreateUserCommand.ChangePasswordQuery -> updateContent { it.copy(passwordQuery = command.query) }
            is CreateUserCommand.ChangeUserNameQuery -> updateContent { it.copy(userNameQuery = command.query) }
            is CreateUserCommand.ChangeUserRoleQuery -> updateContent { it.copy(selectedRole = command.role) }
            CreateUserCommand.CreateUser -> createUser()
            CreateUserCommand.Refresh -> onErrorRefreshClick()
        }
    }

    private fun onErrorRefreshClick() {
        _state.update { state ->
            when (state) {
                is CreateUserUiState.Content -> state.copy(fullScreenError = null)
                else -> CreateUserUiState.Content()
            }
        }
    }

    private fun updateContent(reducer: (CreateUserUiState.Content) -> CreateUserUiState.Content) {
        _state.update { current ->
            val content = (current as? CreateUserUiState.Content) ?: CreateUserUiState.Content()
            val updated = reducer(content)
            updated.copy(canCreate = computeCanCreate(updated))
        }
    }

    private fun computeCanCreate(content: CreateUserUiState.Content): Boolean {
        val username = content.userNameQuery.trim()
        val name = content.nameQuery.trim()
        val password = content.passwordQuery

        val allFilled = username.isNotEmpty() && name.isNotEmpty() && password.isNotEmpty()

        val usernameValid = username.length in 3..64

        val nameValid = name.length in 1..255

        val passwordValid = password.length in 6..128

        return allFilled && usernameValid && nameValid && passwordValid && !content.isCreating
    }


    private fun createUser() {
        val content = _state.value as? CreateUserUiState.Content ?: return
        if (!content.canCreate) return

        viewModelScope.launch {
            _state.value = content.copy(isCreating = true, canCreate = false)

            val userName = content.userNameQuery.trim()
            val name = content.nameQuery.trim()
            val password = content.passwordQuery
            val role = when (content.selectedRole) {
                Role.MANAGER -> "manager"
                Role.EMPLOYEE -> "employee"
            }

            when (val result = createUserUseCase(
                username = userName,
                name = name,
                role = role,
                password = password
            )) {
                is AppResult.Success -> {
                    _events.tryEmit(CreateUserEvent.Back)
                }

                is AppResult.Failure -> {
                    _state.update { state ->
                        val content = (state as? CreateUserUiState.Content) ?: content
                        val base = content.copy(isCreating = false)
                        base.copy(canCreate = computeCanCreate(base))
                    }

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
                if (current is CreateUserUiState.Content) {
                    _state.value = current.copy(fullScreenError = uiError.model, isCreating = false)
                } else {
                    _state.value = CreateUserUiState.Error(uiError.model)
                }
            }
        }
    }
}

sealed interface CreateUserEvent {
    data object Back : CreateUserEvent
}

sealed interface CreateUserCommand {
    data class ChangeUserNameQuery(val query: String) : CreateUserCommand
    data class ChangeNameQuery(val query: String) : CreateUserCommand
    data class ChangePasswordQuery(val query: String) : CreateUserCommand
    data class ChangeUserRoleQuery(val role: Role) : CreateUserCommand
    data object CreateUser : CreateUserCommand
    data object Refresh : CreateUserCommand
}

sealed interface CreateUserUiState {

    data object Initial : CreateUserUiState
    data object Loading : CreateUserUiState

    data class Content(
        val userNameQuery: String = "",
        val nameQuery: String = "",
        val passwordQuery: String = "",
        val selectedRole: Role = Role.EMPLOYEE,
        val canCreate: Boolean = false,
        val isCreating: Boolean = false,
        val fullScreenError: ErrorScreenModel? = null
    ) : CreateUserUiState

    data class Error(
        val model: ErrorScreenModel
    ) : CreateUserUiState
}