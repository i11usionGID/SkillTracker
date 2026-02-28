package com.i11usion.feature.auth.presentation

import AppError
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.i11usion.core.domain.model.network.AppResult
import com.i11usion.core.domain.usecase.auth.CheckAuthUseCase
import com.i11usion.core.domain.usecase.auth.LoginUseCase
import com.i11usion.core.domain.usecase.session.GetUserInfoUseCase
import com.i11usion.core.ui.model.ErrorScreenModel
import com.i11usion.core.ui.model.UiError
import com.i11usion.core.ui.model.UiText
import com.i11usion.feature.auth.presentation.mapper.toAuthUiError
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

class AuthViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase,
    private val checkAuthUseCase: CheckAuthUseCase,
    private val getUserInfoUseCase: GetUserInfoUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(AuthUiState())
    val state = _state.asStateFlow()

    private val _events = MutableSharedFlow<AuthEvent>(extraBufferCapacity = 1)
    val events = _events.asSharedFlow()

    private enum class LastAction { NONE, CHECK_AUTH, LOGIN }
    private var lastAction: LastAction = LastAction.NONE

    init {
        checkAuth()
    }

    private fun checkAuth() {
        lastAction = LastAction.CHECK_AUTH

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, fullScreenError = null) }

            val user = getUserInfoUseCase()
            if (user == null) {
                _state.update { it.copy(isLoading = false) }
                return@launch
            }

            _state.update { it.copy(username = user.username) }

            when (val result = checkAuthUseCase()) {
                is AppResult.Success -> {
                    if (result.data) {
                        _state.update { it.copy(isLoading = false) }
                        _events.tryEmit(AuthEvent.Success)
                    } else {
                        _state.update { it.copy(isLoading = false) }
                    }
                }

                is AppResult.Failure -> {
                    _state.update { it.copy(isLoading = false) }
                    handleError(result.error)
                }
            }
        }
    }

    fun onUsernameChange(value: String) {
        _state.update { it.copy(username = value, fullScreenError = null) }
    }

    fun onPasswordChange(value: String) {
        _state.update { it.copy(password = value, fullScreenError = null) }
    }

    fun onLoginClick() {
        lastAction = LastAction.LOGIN

        val username = _state.value.username.trim()
        val password = _state.value.password

        if (username.isEmpty() || password.isEmpty()) {
            _events.tryEmit(AuthEvent.ShowSnackBar(UiText.Dynamic("Заполните все поля")))
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, fullScreenError = null) }

            when (val result = loginUseCase(username, password)) {
                is AppResult.Success -> {
                    _events.tryEmit(AuthEvent.Success)
                }

                is AppResult.Failure -> {
                    _state.update { it.copy(isLoading = false) }

                    if (result.error is AppError.Unauthorized) {
                        _state.update { it.copy(password = "") }
                    }

                    handleError(result.error)
                }
            }
        }
    }

    fun onErrorRefreshClick() {
        _state.update { it.copy(fullScreenError = null) }

        when (lastAction) {
            LastAction.CHECK_AUTH -> checkAuth()
            LastAction.LOGIN -> onLoginClick()
            LastAction.NONE -> Unit
        }
    }

    private fun handleError(error: AppError) {
        when (val uiError = error.toAuthUiError()) {
            is UiError.SnackBar -> _events.tryEmit(AuthEvent.ShowSnackBar(uiError.message))
            is UiError.FullScreen -> _state.update { it.copy(fullScreenError = uiError.model) }
        }
    }
}

data class AuthUiState(
    val username: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val fullScreenError: ErrorScreenModel? = null
)

sealed interface AuthEvent {
    data object Success : AuthEvent
    data class ShowSnackBar(val message: UiText) : AuthEvent
}

