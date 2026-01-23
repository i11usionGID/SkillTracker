package com.i11usion.feature.auth.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.i11usion.core.domain.usecase.CheckAuthUseCase
import com.i11usion.core.domain.usecase.LoginUseCase
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

class AuthViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase,
    private val checkAuthUseCase: CheckAuthUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(AuthUiState())
    val state = _state.asStateFlow()

    private val _events = MutableSharedFlow<AuthEvent>()
    val events = _events.asSharedFlow()

    init {
        checkAuth()
    }

    private fun checkAuth() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }

            if (checkAuthUseCase()) {
                _events.emit(AuthEvent.Success)
            } else {
                _state.update { it.copy(isLoading = false) }
            }
        }
    }

    fun onUsernameChange(value: String) {
        _state.update { it.copy(username = value) }
    }

    fun onPasswordChange(value: String) {
        _state.update { it.copy(password = value) }
    }

    fun onLoginClick() {
        if (_state.value.username.isEmpty() || _state.value.password.isEmpty()) {
            _state.update { it.copy(error = "Заполните все поля") }
            return
        }
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            runCatching {
                loginUseCase(
                    username = state.value.username,
                    password = state.value.password
                )
            }.onSuccess {
                _events.emit(AuthEvent.Success)
            }.onFailure { error ->
                _state.update {
                    it.copy(
                        isLoading = false,
                        error = error.message
                    )
                }
            }
        }
    }
}

data class AuthUiState(
    val username: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val error: String? = null
)

sealed interface AuthEvent {
    object Success : AuthEvent
}

