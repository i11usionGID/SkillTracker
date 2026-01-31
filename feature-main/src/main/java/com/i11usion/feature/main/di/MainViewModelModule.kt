package com.i11usion.feature.main.di

import androidx.lifecycle.ViewModel
import com.i11usion.di.ViewModelKey
import com.i11usion.feature.main.presentation.HomeViewModel
import com.i11usion.feature.main.presentation.dashboard.DashboardViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
interface MainViewModelModule {
    @Binds
    @IntoMap
    @ViewModelKey(HomeViewModel::class)
    fun bindHomeViewModel(vm: HomeViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(DashboardViewModel::class)
    fun bindDashboardViewModel(vm: DashboardViewModel): ViewModel
}