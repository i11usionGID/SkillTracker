package com.i11usion.core.domain.repository

import com.i11usion.core.domain.model.User

interface SessionRepository {
    fun getAccessToken(): String?
    fun getUserInfo(): User?
    fun saveUserInfo(user: User)
    fun saveAccessToken(token: String)
    fun clear()
}