package com.i11usion.core.domain.repository

interface TokenRepository {
    fun getAccessToken(): String?
    fun saveAccessToken(token: String)
    fun clear()
}