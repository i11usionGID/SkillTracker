package com.i11usion.core.domain.repository

import com.i11usion.core.domain.model.user.User

interface SessionRepository {
    fun getTheme(): Boolean?
    fun switchTheme(): Boolean
    fun getAccessToken(): String?
    fun getUserInfo(): User?
    fun saveUserInfo(user: User)
    fun saveAccessToken(token: String)
    fun clear()
}