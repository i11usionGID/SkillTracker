package com.i11usion.core.ui.snackbar

import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.staticCompositionLocalOf
import kotlinx.coroutines.launch

val LocalAppSnackbarHostState = staticCompositionLocalOf<SnackbarHostState> {
    error("LocalAppSnackbarHostState was not provided")
}

@Composable
fun rememberShowSnackBar(): (String) -> Unit {
    val host = LocalAppSnackbarHostState.current
    val scope = rememberCoroutineScope()

    return { message ->
        scope.launch { host.showSnackbar(message, duration = SnackbarDuration.Short) }
    }
}