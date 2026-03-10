package com.i11usion.core.network.auth

import com.i11usion.core.domain.usecase.session.GetAccessTokenUseCase
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(
    private val getAccessTokenUseCase: GetAccessTokenUseCase
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val path = request.url.encodedPath

        val isAuthEndpoint = path.startsWith("/api/auth") && !path.endsWith("me")

        if (isAuthEndpoint) {
            return chain.proceed(request)
        }

        val token = getAccessTokenUseCase()

        val authorizedRequest = request.newBuilder().apply {
            token?.let {
                addHeader(AUTH, "Bearer $it")
            }
        }.build()

        val response = chain.proceed(authorizedRequest)

        return response
    }

    companion object {
        private const val AUTH = "Authorization"
    }
}