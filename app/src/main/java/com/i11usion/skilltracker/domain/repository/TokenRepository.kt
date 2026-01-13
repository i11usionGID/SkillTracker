package com.i11usion.skilltracker.domain.repository

interface TokenRepository {
    fun getAccessToken(): String?
    fun saveAccessToken(token: String)
    fun clear()
}