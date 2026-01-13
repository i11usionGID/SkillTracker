package com.i11usion.skilltracker.data.auth.local.repository

import android.app.Application
import android.content.Context
import androidx.core.content.edit
import com.i11usion.skilltracker.domain.repository.TokenRepository
import javax.inject.Inject

class TokenRepositoryImpl @Inject constructor(
    context: Application
): TokenRepository {

    private val prefs = context.getSharedPreferences(
        AUTH, Context.MODE_PRIVATE
    )
    override fun getAccessToken(): String? =
        prefs.getString(TOKEN, null)

    override fun saveAccessToken(token: String) {
        prefs.edit { putString(TOKEN, token) }
    }

    override fun clear() {
        prefs.edit { clear() }
    }

    companion object {
        private const val AUTH = "auth"
        private const val TOKEN = "token"
    }
}