package com.i11usion.feature.main.di

import com.i11usion.di.ApplicationScope
import com.i11usion.feature.main.manager.NotificationManager
import com.i11usion.feature.main.manager.SnackbarManager
import dagger.Module
import dagger.Provides

@Module
object ManagersModule {

    @Provides
    @ApplicationScope
    fun provideNotificationManager(): NotificationManager = NotificationManager()

    @Provides
    @ApplicationScope
    fun provideSnackbarManager(): SnackbarManager = SnackbarManager()
}