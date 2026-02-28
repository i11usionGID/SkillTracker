package com.i11usion.feature.auth.di

import androidx.lifecycle.ViewModel
import com.i11usion.di.ViewModelKey
import com.i11usion.feature.auth.presentation.AuthViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
interface AuthViewModelModule {
    @Binds
    @IntoMap
    @ViewModelKey(AuthViewModel::class)
    fun bindAuthViewModel(vm: AuthViewModel): ViewModel
}