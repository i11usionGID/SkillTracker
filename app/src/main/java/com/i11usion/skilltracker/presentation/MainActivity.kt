package com.i11usion.skilltracker.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.Text
import androidx.compose.runtime.CompositionLocalProvider
import com.i11usion.skilltracker.SkillTrackerApp
import com.i11usion.skilltracker.di.LocalViewModelFactory
import com.i11usion.skilltracker.di.ViewModelFactory
import com.i11usion.skilltracker.navigation.AppNavGraph
import com.i11usion.skilltracker.navigation.rememberNavigationState
import com.i11usion.skilltracker.presentation.screen.authentication.AuthScreen
import com.i11usion.skilltracker.presentation.screen.home.HomeRoute
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
                CompositionLocalProvider(
                    LocalViewModelFactory provides viewModelFactory
                ) {
                    val navigationState = rememberNavigationState()

                    AppNavGraph(
                        navigationState = navigationState,
                        authScreen = { onSuccess ->
                            AuthScreen(onSuccessAuth = onSuccess)
                        },
                        dashboardScreen = { HomeRoute() },
                        tasksScreen = { Text("Tasks") },
                        usersScreen = { Text("Users") },
                        testsScreen = { Text("Tests") }
                    )
                }
            }
        }
    }
}


//class MainActivity : ComponentActivity() {
//
//    @Inject
//    lateinit var viewModelFactory: ViewModelFactory
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//
//        (application as SkillTrackerApp)
//            .component
//            .inject(this)
//
//        super.onCreate(savedInstanceState)
//        enableEdgeToEdge()
//        setContent {
//            SkillTrackerTheme {
//                NavGraph(viewModelFactory = viewModelFactory)
//            }
//        }
//    }
//}
//
//package com.i11usion.skilltracker.presentation
//
//import android.os.Bundle
//import androidx.activity.ComponentActivity
//import androidx.activity.compose.setContent
//import androidx.activity.enableEdgeToEdge
//import androidx.compose.runtime.Composable
//import androidx.compose.runtime.collectAsState
//import androidx.compose.runtime.getValue
//import com.i11usion.skilltracker.SkillTrackerApp
//import com.i11usion.skilltracker.di.ViewModelFactory
//import com.i11usion.skilltracker.navigation.AppNavGraph
//import com.i11usion.skilltracker.navigation.NavGraph
//import com.i11usion.skilltracker.navigation.rememberNavigationState
//import com.i11usion.skilltracker.presentation.screen.authentication.AuthScreen
//import com.i11usion.skilltracker.presentation.screen.home.HomeScreen
//import com.i11usion.skilltracker.presentation.screen.home.HomeViewModel
//import com.i11usion.skilltracker.presentation.ui.theme.SkillTrackerTheme
//import javax.inject.Inject
//
//class MainActivity : ComponentActivity() {
//
//    @Inject
//    lateinit var viewModelFactory: ViewModelFactory
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//
//        (application as SkillTrackerApp)
//            .component
//            .inject(this)
//
//        super.onCreate(savedInstanceState)
//        enableEdgeToEdge()
//        setContent {
//            SkillTrackerTheme {
//                NavGraph(viewModelFactory = viewModelFactory)
//            }
//        }
//    }
//
//    @Composable
//    fun MainContent(
//        homeViewModel: HomeViewModel
//    ) {
//        val navigationState = rememberNavigationState()
//        val homeState by homeViewModel.state.collectAsState()
//
//        AppNavGraph(
//            navigationState = navigationState,
//            homeViewModel = homeViewModel,
//            authScreen = { AuthScreen() },
//            dashboardScreen = {
//                HomeScreen(
//                    state = homeState,
//                    onDrawerItemClick = homeViewModel::onDrawerItemClick,
//                    onLogoutClick = homeViewModel::onLogoutClick
//                )
//            },
//            tasksScreen = { TODO() },
//            usersScreen = { TODO() },
//            testsScreen = { TODO() }
//        )
//    }
//
//}