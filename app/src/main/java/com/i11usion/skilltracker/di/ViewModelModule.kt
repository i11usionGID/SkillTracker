package com.i11usion.skilltracker.di

import androidx.lifecycle.ViewModel
import com.i11usion.skilltracker.presentation.screen.authentication.AuthViewModel
import com.i11usion.skilltracker.presentation.screen.home.HomeViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
interface ViewModelModule {

    @IntoMap
    @ViewModelKey(AuthViewModel::class)
    @Binds
    fun bindAuthViewModel(viewModel: AuthViewModel): ViewModel

    @IntoMap
    @ViewModelKey(HomeViewModel::class)
    @Binds
    fun bindHomeViewModel(viewModel: HomeViewModel): ViewModel
}