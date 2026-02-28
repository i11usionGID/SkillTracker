package com.i11usion.core.ui.model

sealed interface UiError {
    data class FullScreen(val model: ErrorScreenModel) : UiError
    data class SnackBar(val message: UiText) : UiError
}