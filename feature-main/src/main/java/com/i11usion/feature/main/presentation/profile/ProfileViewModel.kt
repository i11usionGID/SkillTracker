package com.i11usion.feature.main.presentation.profile

import AppError
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.i11usion.core.domain.model.network.AppResult
import com.i11usion.core.domain.usecase.auth.LoadProfileUseCase
import com.i11usion.core.domain.usecase.user.DeleteUserUseCase
import com.i11usion.core.domain.usecase.user.UpdatePasswordUseCase
import com.i11usion.core.ui.formatter.formatInstantToStringData
import com.i11usion.core.ui.model.ErrorScreenModel
import com.i11usion.core.ui.model.UiError
import com.i11usion.core.ui.model.UiText
import com.i11usion.feature.main.manager.SnackbarManager
import com.i11usion.feature.main.mapper.toScreenUiError
import com.i11usion.feature.main.presentation.dashboard.mapper.userToUserUiModel
import com.i11usion.feature.main.presentation.user.list.model.UserUiModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

class ProfileViewModel @Inject constructor(
    private val loadProfileUseCase: LoadProfileUseCase,
    private val deleteUserUseCase: DeleteUserUseCase,
    private val updatePasswordUseCase: UpdatePasswordUseCase,
    private val snackbarManager: SnackbarManager
) : ViewModel() {

    private companion object {
        const val RETRY_DELAY_MS = 10_000L
    }

    private val _state = MutableStateFlow<ProfileUiState>(ProfileUiState.Initial)
    val state = _state.asStateFlow()

    private val _events = MutableSharedFlow<ProfileEvent>()
    val events = _events.asSharedFlow()

    init {
        loadProfileData()
    }

    fun processCommand(command: ProfileCommand) {
        when (command) {
            ProfileCommand.Refresh -> onErrorRefreshClick()
            is ProfileCommand.Delete -> deleteAccount(command.userId)
            is ProfileCommand.InputPasswordQuery -> {
                updateContent { it.copy(passwordQuery = command.query) }
            }

            is ProfileCommand.InputRepeatPasswordQuery -> {
                updateContent { it.copy(repeatPasswordQuery = command.query) }
            }

            ProfileCommand.UpdatePassword -> updatePassword()
            ProfileCommand.ClearPasswordInputs -> {
                updateContent {
                    it.copy(
                        passwordQuery = "",
                        repeatPasswordQuery = ""
                    )
                }
            }
        }
    }

    private fun onErrorRefreshClick() {
        when (val current = _state.value) {
            is ProfileUiState.Content -> {
                _state.value = current.copy(fullScreenError = null)
            }

            else -> loadProfileData()
        }
    }

    private fun loadProfileData() {
        _state.value = ProfileUiState.Loading

        viewModelScope.launch {
            when (val result = retryProfileRequest()) {
                is AppResult.Success -> {
                    _state.value = ProfileUiState.Content(
                        user = result.data.userToUserUiModel {
                            formatInstantToStringData(it)
                        },
                        fullScreenError = null
                    )
                }

                is AppResult.Failure -> {
                    showUiError(result.error)
                }
            }
        }
    }

    private fun deleteAccount(userId: Int) {
        _state.value = ProfileUiState.Loading

        viewModelScope.launch {
            when (val result = deleteUserUseCase(userId)) {
                is AppResult.Success -> {
                    _events.emit(ProfileEvent.AccountDeleted)
                }

                is AppResult.Failure -> {
                    showUiError(result.error)
                }
            }
        }
    }

    private fun updatePassword() {
        val current = _state.value as? ProfileUiState.Content ?: return

        val validationError = validatePassword(
            passwordQuery = current.passwordQuery,
            repeatPasswordQuery = current.repeatPasswordQuery
        )

        if (validationError != null) {
            updateContent { it }
            return
        }

        updateContent {
            it.copy(
                isUpdatingPassword = true,
                fullScreenError = null
            )
        }

        viewModelScope.launch {
            when (
                val result = updatePasswordUseCase(
                    userId = current.user.id,
                    username = current.user.username,
                    name = current.user.name,
                    role = current.user.role,
                    password = current.passwordQuery.trim()
                )
            ) {
                is AppResult.Success -> {
                    updateContent {
                        it.copy(
                            passwordQuery = "",
                            repeatPasswordQuery = "",
                            isUpdatingPassword = false
                        )
                    }
                    _events.emit(ProfileEvent.PasswordUpdated)
                }

                is AppResult.Failure -> {
                    updateContent { it.copy(isUpdatingPassword = false) }
                    showUiError(result.error)
                }
            }
        }
    }

    private suspend fun retryProfileRequest() = retryRequest {
        loadProfileUseCase()
    }

    private fun updateContent(
        reducer: (ProfileUiState.Content) -> ProfileUiState.Content
    ) {
        _state.update { current ->
            val content = current as? ProfileUiState.Content ?: return@update current
            val updated = reducer(content)

            val createError = validatePassword(
                passwordQuery = updated.passwordQuery,
                repeatPasswordQuery = updated.repeatPasswordQuery
            )

            updated.copy(
                createError = createError,
                canUpdate = createError == null && !updated.isUpdatingPassword &&
                        updated.passwordQuery.isNotBlank() && updated.repeatPasswordQuery.isNotBlank()
            )
        }
    }

    private fun validatePassword(
        passwordQuery: String,
        repeatPasswordQuery: String
    ): String? {
        val password = passwordQuery.trim()
        val repeatPassword = repeatPasswordQuery.trim()

        return when {
            password.isEmpty() && repeatPassword.isEmpty() -> null
            password.isEmpty() -> "Введите новый пароль"
            password.length < 8 -> "Пароль должен содержать минимум 8 символов"
            password.length > 128 -> "Пароль не должен превышать 128 символов"
            repeatPassword.isEmpty() -> "Повторите пароль"
            password != repeatPassword -> "Пароли не совпадают"
            else -> null
        }
    }

    private suspend fun <T> retryRequest(
        block: suspend () -> AppResult<T>
    ): AppResult<T> {
        while (true) {
            when (val result = block()) {
                is AppResult.Success -> return result
                is AppResult.Failure -> {
                    if (!shouldRetry(result.error)) return result
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
                if (current is ProfileUiState.Content) {
                    _state.value = current.copy(fullScreenError = uiError.model)
                } else {
                    _state.value = ProfileUiState.Error(uiError.model)
                }
            }
        }
    }
}

sealed interface ProfileEvent {
    data object AccountDeleted : ProfileEvent
    data object PasswordUpdated : ProfileEvent
}

sealed interface ProfileCommand {
    data object Refresh : ProfileCommand
    data class Delete(val userId: Int) : ProfileCommand
    data object UpdatePassword : ProfileCommand
    data object ClearPasswordInputs : ProfileCommand
    data class InputPasswordQuery(val query: String) : ProfileCommand
    data class InputRepeatPasswordQuery(val query: String) : ProfileCommand
}

sealed interface ProfileUiState {
    data object Initial : ProfileUiState
    data object Loading : ProfileUiState

    data class Content(
        val user: UserUiModel,
        val passwordQuery: String = "",
        val repeatPasswordQuery: String = "",
        val isUpdatingPassword: Boolean = false,
        val fullScreenError: ErrorScreenModel? = null,
        val canUpdate: Boolean = false,
        val createError: String? = null
    ) : ProfileUiState

    data class Error(
        val model: ErrorScreenModel
    ) : ProfileUiState
}