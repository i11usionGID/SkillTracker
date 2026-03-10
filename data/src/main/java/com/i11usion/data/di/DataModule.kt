package com.i11usion.data.di

import android.app.Application
import com.i11usion.core.domain.repository.AuthRepository
import com.i11usion.core.domain.repository.NetworkErrorHandler
import com.i11usion.core.domain.repository.NotificationRepository
import com.i11usion.core.domain.repository.QuizRepository
import com.i11usion.core.domain.repository.SessionManager
import com.i11usion.core.domain.repository.SessionRepository
import com.i11usion.core.domain.repository.TaskRepository
import com.i11usion.core.domain.repository.UserRepository
import com.i11usion.data.local.repository.NetworkErrorHandlerImpl
import com.i11usion.data.local.repository.SessionManagerImpl
import com.i11usion.data.local.repository.SessionRepositoryImpl
import com.i11usion.data.remote.repository.AuthRepositoryImpl
import com.i11usion.data.remote.repository.NotificationRepositoryImpl
import com.i11usion.data.remote.repository.QuizRepositoryImpl
import com.i11usion.data.remote.repository.TaskRepositoryImpl
import com.i11usion.data.remote.repository.UserRepositoryImpl
import com.i11usion.di.ApplicationScope
import dagger.Binds
import dagger.Module
import dagger.Provides

@Module
interface DataModule {

    @ApplicationScope
    @Binds
    fun bindAuthRepository(impl: AuthRepositoryImpl): AuthRepository

    @ApplicationScope
    @Binds
    fun bindTaskRepository(impl: TaskRepositoryImpl): TaskRepository

    @ApplicationScope
    @Binds
    fun bindUserRepository(impl: UserRepositoryImpl): UserRepository

    @ApplicationScope
    @Binds
    fun bindQuizRepository(impl: QuizRepositoryImpl): QuizRepository

    @ApplicationScope
    @Binds
    fun bindNotificationRepository(impl: NotificationRepositoryImpl): NotificationRepository

    @ApplicationScope
    @Binds
    fun bindSessionManager(impl: SessionManagerImpl): SessionManager

    @ApplicationScope
    @Binds
    fun bindNetworkErrorHandler(impl: NetworkErrorHandlerImpl): NetworkErrorHandler

    companion object {
        @ApplicationScope
        @Provides
        fun provideSessionRepository(
            application: Application
        ): SessionRepository = SessionRepositoryImpl(application)
    }
}