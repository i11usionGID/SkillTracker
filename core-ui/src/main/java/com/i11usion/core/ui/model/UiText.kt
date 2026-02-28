package com.i11usion.core.ui.model

sealed interface UiText {
    data class Res(val id: Int) : UiText
    data class Dynamic(val value: String) : UiText
}