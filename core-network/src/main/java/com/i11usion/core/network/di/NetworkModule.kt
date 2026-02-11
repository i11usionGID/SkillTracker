package com.i11usion.core.network.di

import com.i11usion.core.domain.usecase.session.GetAccessTokenUseCase
import com.i11usion.core.network.auth.AuthInterceptor
import com.i11usion.core.network.auth.AuthService
import com.i11usion.core.network.quiz.QuizService
import com.i11usion.core.network.task.TaskService
import com.i11usion.core.network.users.UserService
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
        getAccessTokenUseCase: GetAccessTokenUseCase
    ): AuthInterceptor =
        AuthInterceptor(getAccessTokenUseCase)

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
    fun provideAuthService(
        retrofit: Retrofit
    ): AuthService =
        retrofit.create(AuthService::class.java)

    @ApplicationScope
    @Provides
    fun provideTaskService(
        retrofit: Retrofit
    ): TaskService =
        retrofit.create(TaskService::class.java)

    @ApplicationScope
    @Provides
    fun provideUsersService(
        retrofit: Retrofit
    ): UserService =
        retrofit.create(UserService::class.java)

    @ApplicationScope
    @Provides
    fun provideQuizService(
        retrofit: Retrofit
    ): QuizService =
        retrofit.create(QuizService::class.java)
}