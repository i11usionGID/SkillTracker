package com.i11usion.skilltracker.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.i11usion.skilltracker.SkillTrackerApp
import com.i11usion.skilltracker.di.ViewModelFactory
import com.i11usion.skilltracker.navigation.NavGraph
import com.i11usion.skilltracker.presentation.ui.theme.SkillTrackerTheme
import javax.inject.Inject

class MainActivity : ComponentActivity() {

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    override fun onCreate(savedInstanceState: Bundle?) {

        (application as SkillTrackerApp)
            .component
            .inject(this)

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SkillTrackerTheme {
                NavGraph(viewModelFactory = viewModelFactory)
            }
        }
    }
}