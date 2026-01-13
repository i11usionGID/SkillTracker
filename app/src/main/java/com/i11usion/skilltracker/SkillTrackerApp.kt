package com.i11usion.skilltracker

import android.app.Application
import com.i11usion.skilltracker.di.ApplicationComponent
import com.i11usion.skilltracker.di.DaggerApplicationComponent

class SkillTrackerApp : Application() {

    val component: ApplicationComponent by lazy {
        DaggerApplicationComponent.factory().create(this)
    }
}
