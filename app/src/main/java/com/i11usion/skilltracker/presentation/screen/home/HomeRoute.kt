package com.i11usion.skilltracker.presentation.screen.home

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.viewmodel.compose.viewModel
import com.i11usion.skilltracker.di.LocalViewModelFactory

@Composable
fun HomeRoute() {
    val viewModel: HomeViewModel = viewModel(factory = LocalViewModelFactory.current)
    val state = viewModel.state.collectAsState().value

    HomeScreen(
        state = state,
        onDrawerItemClick = viewModel::onDrawerItemClick,
        onLogoutClick = viewModel::onLogoutClick
    )
}
