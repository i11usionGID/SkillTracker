package com.i11usion.feature.main.di

import androidx.lifecycle.ViewModel
import com.i11usion.di.ViewModelKey
import com.i11usion.feature.main.presentation.HomeViewModel
import com.i11usion.feature.main.presentation.dashboard.DashboardViewModel
import com.i11usion.feature.main.presentation.quiz.QuizViewModel
import com.i11usion.feature.main.presentation.task.TaskViewModel
import com.i11usion.feature.main.presentation.user.UserViewModel
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

    @Binds
    @IntoMap
    @ViewModelKey(TaskViewModel::class)
    fun bindTaskViewModel(vm: TaskViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(UserViewModel::class)
    fun bindUserViewModel(vm: UserViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(QuizViewModel::class)
    fun bindQuizViewModel(vm: QuizViewModel): ViewModel
}