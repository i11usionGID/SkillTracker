package com.i11usion.core.ui.model

data class ErrorScreenModel(
    val iconResId: Int,
    val cdResId: Int,
    val text: UiText,
    val showBack: Boolean = false,
)