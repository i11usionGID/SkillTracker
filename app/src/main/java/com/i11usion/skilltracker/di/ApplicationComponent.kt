package com.i11usion.skilltracker.di

import android.app.Application
import com.i11usion.core.network.di.NetworkModule
import com.i11usion.data.di.DataModule
import com.i11usion.di.ApplicationScope
import com.i11usion.feature.auth.di.AuthViewModelModule
import com.i11usion.feature.main.di.MainViewModelModule
import com.i11usion.feature.main.di.ManagersModule
import com.i11usion.skilltracker.presentation.MainActivity
import dagger.BindsInstance
import dagger.Component

@ApplicationScope
@Component(
    modules = [
        NetworkModule::class,
        DataModule::class,
        AuthViewModelModule::class,
        MainViewModelModule::class,
        ManagersModule::class
    ]
)
interface ApplicationComponent {

    fun inject(activity: MainActivity)

    @Component.Factory
    interface Factory {
        fun create(
            @BindsInstance application: Application
        ): ApplicationComponent
    }
}