package com.i11usion.data.local.repository

import android.app.Application
import android.content.Context
import androidx.core.content.edit
import com.i11usion.core.domain.model.Role
import com.i11usion.core.domain.model.User
import com.i11usion.core.domain.repository.SessionRepository
import javax.inject.Inject

class SessionRepositoryImpl @Inject constructor(
    context: Application
) : SessionRepository {

    private val prefs = context.getSharedPreferences(AUTH, Context.MODE_PRIVATE)

    override fun getAccessToken(): String? =
        prefs.getString(TOKEN, null)

    override fun getUserInfo(): User? {
        val name = prefs.getString(USER_NAME, null) ?: return null
        val username = prefs.getString(USER_USERNAME, null) ?: return null
        val role = prefs.getString(USER_ROLE, null)
            ?.let { runCatching { Role.valueOf(it) }.getOrNull() }
            ?: return null

        return User(
            id = prefs.getInt(USER_ID, 0),
            name = name,
            username = username,
            role = role
        )
    }

    override fun saveUserInfo(user: User) {
        prefs.edit {
            putInt(USER_ID, user.id)
            putString(USER_NAME, user.name)
            putString(USER_USERNAME, user.username)
            putString(USER_ROLE, user.role.name)
        }
    }

    override fun saveAccessToken(token: String) {
        prefs.edit { putString(TOKEN, token) }
    }

    override fun clear() {
        prefs.edit { clear() }
    }

    companion object {
        private const val AUTH = "auth"
        private const val TOKEN = "token"
        private const val USER_ID = "user_id"
        private const val USER_NAME = "user_name"
        private const val USER_USERNAME = "user_username"
        private const val USER_ROLE = "user_role"
    }
}
