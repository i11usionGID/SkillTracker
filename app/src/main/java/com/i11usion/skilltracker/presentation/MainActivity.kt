package com.i11usion.skilltracker.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableStateOf
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.i11usion.core.domain.repository.SessionManager
import com.i11usion.core.domain.repository.SessionRepository
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

    @Inject
    lateinit var sessionManager: SessionManager

    @Inject
    lateinit var sessionRepository: SessionRepository

    private val isDarkTheme = mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()

        (application as SkillTrackerApp)
            .component
            .inject(this)

        isDarkTheme.value = sessionRepository.getTheme() ?: false

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            SkillTrackerTheme(
                darkTheme = isDarkTheme.value
            ) {
                CompositionLocalProvider(
                    LocalViewModelFactory provides viewModelFactory
                ) {
                    val navigationState = rememberNavigationState()

                    AppNavGraph(
                        navigationState = navigationState,
                        sessionManager = sessionManager,
                        authScreen = { onSuccess ->
                            AuthScreen(onSuccessAuth = onSuccess)
                        },
                        homeScreen = { onLogout ->
                            HomeRoute(
                                isDarkTheme = isDarkTheme.value,
                                onLogoutNavigate = onLogout,
                                onSwitchThemeClick = {
                                    isDarkTheme.value = sessionRepository.switchTheme()
                                }
                            )
                        }
                    )
                }
            }
        }
    }
}