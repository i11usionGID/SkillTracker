package com.i11usion.skilltracker.di

import android.app.Application
import com.i11usion.skilltracker.data.auth.local.repository.TokenRepositoryImpl
import com.i11usion.skilltracker.data.auth.remote.AuthInterceptor
import com.i11usion.skilltracker.data.auth.remote.AuthService
import com.i11usion.skilltracker.data.auth.remote.repository.AuthRepositoryImpl
import com.i11usion.skilltracker.domain.repository.AuthRepository
import com.i11usion.skilltracker.domain.repository.TokenRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

@Module
interface DataModule {

    @ApplicationScope
    @Binds
    fun bindAuthRepository(impl: AuthRepositoryImpl): AuthRepository

    companion object {
        @ApplicationScope
        @Provides
        fun provideTokenRepository(
            application: Application
        ): TokenRepository = TokenRepositoryImpl(application)

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
}
