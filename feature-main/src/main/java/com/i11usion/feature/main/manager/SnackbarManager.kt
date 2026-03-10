package com.i11usion.feature.main.manager

import com.i11usion.core.ui.model.UiText
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import javax.inject.Inject

class SnackbarManager @Inject constructor() {
    private val _events = MutableSharedFlow<UiText>(
        replay = 0,
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val events: SharedFlow<UiText> = _events

    fun show(message: UiText) {
        _events.tryEmit(message)
    }
}