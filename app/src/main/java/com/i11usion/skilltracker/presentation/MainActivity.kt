package com.i11usion.skilltracker.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.CompositionLocalProvider
import com.i11usion.core.ui.di.LocalViewModelFactory
import com.i11usion.core.ui.theme.SkillTrackerTheme
import com.i11usion.di.ViewModelFactory
import com.i11usion.feature.auth.presentation.AuthScreen
import com.i11usion.feature.main.presentation.HomeRoute
import com.i11usion.skilltracker.SkillTrackerApp
import com.i11usion.skilltracker.navigation.AppNavGraph
import com.i11usion.skilltracker.navigation.rememberNavigationState
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
                CompositionLocalProvider(
                    LocalViewModelFactory provides viewModelFactory
                ) {
                    val navigationState = rememberNavigationState()

                    AppNavGraph(
                        navigationState = navigationState,
                        authScreen = { onSuccess ->
                            AuthScreen(onSuccessAuth = onSuccess)
                        },
                        homeScreen = { onLogout ->
                            HomeRoute(onLogoutNavigate = onLogout)
                        }
                    )
                }
            }
        }
    }
}