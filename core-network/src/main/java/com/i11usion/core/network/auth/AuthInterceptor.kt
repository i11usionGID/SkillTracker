package com.i11usion.core.network.auth

import com.i11usion.core.domain.usecase.GetAccessTokenUseCase
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

        val authorizedRequest = if (token != null) {
            request.newBuilder()
                .addHeader(AUTH, "Bearer $token")
                .build()
        } else {
            request
        }

        return chain.proceed(authorizedRequest)
    }

    companion object {
        private const val AUTH = "Authorization"
    }
}