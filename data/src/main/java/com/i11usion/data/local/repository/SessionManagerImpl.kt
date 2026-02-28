package com.i11usion.data.local.repository

import android.util.Log
import com.i11usion.core.domain.repository.SessionManager
import com.i11usion.core.domain.repository.SessionRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

class SessionManagerImpl @Inject constructor(
    private val tokenStorage: SessionRepository
) : SessionManager {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val _sessionExpiredFlow = MutableSharedFlow<Unit>(
        replay = 0,
        extraBufferCapacity = 1
    )

    override val sessionExpiredFlow = _sessionExpiredFlow.asSharedFlow()

    @Volatile
    private var alreadyExpired = false

    override fun notifySessionExpired() {
        if (!alreadyExpired) {
            alreadyExpired = true
            scope.launch {
                Log.d("Debug401", "вызван метод notifySessionExpired")
                tokenStorage.clear()
                _sessionExpiredFlow.tryEmit(Unit)
                delay(1000)
                alreadyExpired = false
            }
        }
    }
}