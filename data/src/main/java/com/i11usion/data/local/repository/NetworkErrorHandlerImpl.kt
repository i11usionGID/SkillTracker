package com.i11usion.data.local.repository

import com.i11usion.core.domain.repository.NetworkErrorHandler
import com.i11usion.core.domain.repository.SessionManager
import javax.inject.Inject

class NetworkErrorHandlerImpl @Inject constructor(
    private val sessionManager: SessionManager
) : NetworkErrorHandler {

    override fun onUnauthorized() {
        sessionManager.notifySessionExpired()
    }
}