package com.i11usion.core.ui.di

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.lifecycle.ViewModelProvider

val LocalViewModelFactory =
staticCompositionLocalOf<ViewModelProvider.Factory> {
    error("ViewModelFactory not provided")
}
