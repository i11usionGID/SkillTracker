package com.i11usion.feature.main.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.i11usion.core.domain.model.Role
import com.i11usion.core.domain.usecase.LoadProfileUseCase
import com.i11usion.core.navigation.Screen
import com.i11usion.feature.main.presentation.drawer.DrawerConfig
import com.i11usion.feature.main.presentation.drawer.DrawerItem
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

class HomeViewModel @Inject constructor(
    private val loadProfileUseCase: LoadProfileUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(HomeUiState())
    val state = _state.asStateFlow()

    private val _events = MutableSharedFlow<HomeEvent>()
    val events = _events.asSharedFlow()

    init {
        loadUser()
    }

    fun onDrawerItemClick(screen: Screen) {
        _state.update { it.copy(selectedScreen = screen) }
        viewModelScope.launch { _events.emit(HomeEvent.Navigate(screen)) }
    }


    fun onLogoutClick() {
        viewModelScope.launch {
            _events.emit(HomeEvent.Logout)
        }
    }

    private fun loadUser() {
        viewModelScope.launch {
            val user = loadProfileUseCase()

            _state.update {
                val filtered = DrawerConfig.items.filter { user.role in it.allowedRoles }
                it.copy(
                    userName = user.name,
                    role = user.role,
                    drawerItems = filtered,
                    selectedScreen = filtered.firstOrNull()?.screen
                )
            }
        }
    }
}


data class HomeUiState(
    val userName: String = "",
    val role: Role? = null,
    val drawerItems: List<DrawerItem> = emptyList(),
    val selectedScreen: Screen? = null
)

sealed interface HomeEvent {
    data class Navigate(val screen: Screen) : HomeEvent
    object Logout : HomeEvent
}

