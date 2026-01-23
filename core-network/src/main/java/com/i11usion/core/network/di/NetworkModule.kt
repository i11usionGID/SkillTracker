package com.i11usion.core.network.di

import com.i11usion.core.domain.repository.TokenRepository
import com.i11usion.core.network.AuthInterceptor
import com.i11usion.core.network.AuthService
import com.i11usion.di.ApplicationScope
import dagger.Module
import dagger.Provides
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

@Module
object NetworkModule {
    @ApplicationScope
    @Provides
    fun provideLoggingInterceptor(): HttpLoggingInterceptor =
        HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

    @ApplicationScope
    @Provides
    fun provideAuthInterceptor(
        tokenRepository: TokenRepository
    ): AuthInterceptor =
        AuthInterceptor(tokenRepository)

    @ApplicationScope
    @Provides
    fun provideOkHttpClient(
        authInterceptor: AuthInterceptor,
        loggingInterceptor: HttpLoggingInterceptor
    ): OkHttpClient =
        OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(loggingInterceptor)
            .build()

    @ApplicationScope
    @Provides
    fun provideRetrofit(
        okHttpClient: OkHttpClient
    ): Retrofit =
        Retrofit.Builder()
            .baseUrl("https://apiskilltracker.tw1.ru/")
            .addConverterFactory(GsonConverterFactory.create())
            .client(okHttpClient)
            .build()

    @ApplicationScope
    @Provides
    fun provideAuthApi(
        retrofit: Retrofit
    ): AuthService =
        retrofit.create(AuthService::class.java)
}