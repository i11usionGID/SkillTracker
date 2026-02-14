package com.i11usion.feature.main.presentation.user.create

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.i11usion.core.domain.model.user.Role
import com.i11usion.core.domain.usecase.user.CreateUserUseCase
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

class CreateUserViewModel @Inject constructor(
    private val createUserUseCase: CreateUserUseCase
) : ViewModel() {

    private val _state = MutableStateFlow<CreateUserUiState>(CreateUserUiState.Content())
    val state = _state.asStateFlow()

    private val _events = MutableSharedFlow<Unit>()
    val events = _events.asSharedFlow()

    fun processCommand(command: CreateUserCommand) {
        when (command) {
            is CreateUserCommand.ChangeNameQuery -> updateContent { it.copy(nameQuery = command.query) }
            is CreateUserCommand.ChangePasswordQuery -> updateContent { it.copy(passwordQuery = command.query) }
            is CreateUserCommand.ChangeUserNameQuery -> updateContent { it.copy(userNameQuery = command.query) }
            is CreateUserCommand.ChangeUserRoleQuery -> updateContent { it.copy(selectedRole = command.role) }
            CreateUserCommand.CreateUser -> createUser()
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

        val usernameValid = username.length in 5..14

        val nameValid = name.length >= 5

        val passwordValid = password.length >= 8

        return allFilled && usernameValid && nameValid && passwordValid && !content.isCreating
    }


    private fun createUser() {
        val content = _state.value as? CreateUserUiState.Content ?: return
        if (!content.canCreate) return

        viewModelScope.launch {
            _state.value = content.copy(isCreating = true, canCreate = false)

            try {
                val userName = content.userNameQuery.trim()
                val name = content.nameQuery.trim()
                val password = content.passwordQuery
                val role = when (content.selectedRole) {
                    Role.MANAGER -> "manager"
                    Role.EMPLOYEE -> "employee"
                }

                createUserUseCase(
                    username = userName,
                    name = name,
                    role = role,
                    password = password
                )

                _events.emit(Unit)
            } catch (t: Throwable) {
                _state.value = CreateUserUiState.Error(t.message ?: "Failed to create user")
            }
        }
    }
}

sealed interface CreateUserCommand {
    data class ChangeUserNameQuery(val query: String) : CreateUserCommand
    data class ChangeNameQuery(val query: String) : CreateUserCommand
    data class ChangePasswordQuery(val query: String) : CreateUserCommand
    data class ChangeUserRoleQuery(val role: Role) : CreateUserCommand
    data object CreateUser : CreateUserCommand
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
        val isCreating: Boolean = false
    ) : CreateUserUiState

    data class Error(
        val message: String
    ) : CreateUserUiState
}