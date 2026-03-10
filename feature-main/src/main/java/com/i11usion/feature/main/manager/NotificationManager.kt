package com.i11usion.feature.main.manager

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import javax.inject.Inject

class NotificationManager @Inject constructor() {
    private val _events = MutableSharedFlow<Unit>()
    val events: SharedFlow<Unit> = _events

    suspend fun readNotification() {
        _events.emit(Unit)
    }
}