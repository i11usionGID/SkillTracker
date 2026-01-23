package com.i11usion.data.di

import android.app.Application
import com.i11usion.core.domain.repository.AuthRepository
import com.i11usion.core.domain.repository.TokenRepository
import com.i11usion.data.local.repository.TokenRepositoryImpl
import com.i11usion.data.remote.repository.AuthRepositoryImpl
import com.i11usion.di.ApplicationScope
import dagger.Binds
import dagger.Module
import dagger.Provides

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
    }
}