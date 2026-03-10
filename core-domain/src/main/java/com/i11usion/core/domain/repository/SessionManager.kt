package com.i11usion.core.domain.repository

import kotlinx.coroutines.flow.SharedFlow

interface SessionManager {
    val sessionExpiredFlow: SharedFlow<Unit>
    fun notifySessionExpired()
}