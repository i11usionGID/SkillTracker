package com.i11usion.feature.auth.presentation

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.i11usion.core.ui.di.LocalViewModelFactory
import com.i11usion.core.ui.model.UiText
import com.i11usion.core.ui.template.ErrorScreen
import com.i11usion.core.ui.template.Logo
import com.i11usion.feature.auth.R
import kotlinx.coroutines.flow.collectLatest

@Composable
fun AuthScreen(
    modifier: Modifier = Modifier,
    onSuccessAuth: () -> Unit
) {
    val viewModel: AuthViewModel = viewModel(factory = LocalViewModelFactory.current)
    val state = viewModel.state.collectAsState().value

    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.events.collectLatest { effect ->
            when (effect) {
                AuthEvent.Success -> onSuccessAuth()

                is AuthEvent.ShowSnackBar -> {
                    snackbarHostState.showSnackbar(
                        message = effect.message.asString(context),
                        duration = SnackbarDuration.Short
                    )
                }
            }
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.surface,
        snackbarHost = {
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier
                    .fillMaxWidth()
                    .imePadding()
                    .navigationBarsPadding()
            ) { data ->
                Snackbar(
                    snackbarData = data,
                    shape = RoundedCornerShape(16.dp),
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        }
    ) { padding ->

        val fsError = state.fullScreenError
        if (fsError != null) {
            ErrorScreen(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .imePadding()
                    .navigationBarsPadding()
                    .verticalScroll(rememberScrollState()),
                iconResId = fsError.iconResId,
                cdResId = fsError.cdResId,
                errorText = fsError.text.asString(context),
                onRefreshClick = { viewModel.onErrorRefreshClick() }
            )
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(color = MaterialTheme.colorScheme.surface),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Logo()
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = stringResource(com.i11usion.core.ui.R.string.application_name),
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = stringResource(R.string.log_in_to_your_acc),
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(24.dp))

            Surface(
                modifier = Modifier.padding(horizontal = 16.dp),
                shape = RoundedCornerShape(28.dp),
                color = MaterialTheme.colorScheme.surfaceContainerLowest,
                shadowElevation = 4.dp
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {

                    Text(
                        text = stringResource(R.string.username),
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    InputField(
                        placeholderText = "username",
                        isLoading = state.isLoading,
                        isPassword = false,
                        query = state.username,
                        onQueryChanged = viewModel::onUsernameChange
                    )

                    Text(
                        text = stringResource(R.string.password),
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    InputField(
                        placeholderText = "••••••••",
                        isLoading = state.isLoading,
                        isPassword = true,
                        query = state.password,
                        onQueryChanged = viewModel::onPasswordChange
                    )

                    if (state.isLoading) {
                        LoginButton(
                            enabled = false,
                            colors = ButtonDefaults.buttonColors(
                                disabledContainerColor = MaterialTheme.colorScheme.primary.copy(
                                    alpha = 0.7f
                                )
                            )
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = Color.White,
                                strokeWidth = 1.dp
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = stringResource(R.string.loading),
                                color = Color.White,
                                fontSize = 16.sp
                            )
                        }
                    } else {
                        LoginButton(
                            onLoginButtonClick = viewModel::onLoginClick,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Text(
                                text = stringResource(R.string.login),
                                color = Color.White,
                                fontSize = 16.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun InputField(
    modifier: Modifier = Modifier,
    isLoading: Boolean,
    isPassword: Boolean,
    placeholderText: String,
    query: String,
    onQueryChanged: (String) -> Unit
) {

    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    OutlinedTextField(
        value = query,
        onValueChange = onQueryChanged,
        enabled = !isLoading,
        placeholder = {
            if (!isFocused) {
                Text(text = placeholderText)
            }
        },
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        singleLine = true,
        interactionSource = interactionSource,
        visualTransformation = if (isPassword) {
            PasswordVisualTransformation()
        } else VisualTransformation.None,
        colors = OutlinedTextFieldDefaults.colors(
            unfocusedBorderColor = MaterialTheme.colorScheme.surfaceBright,
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedPlaceholderColor = MaterialTheme.colorScheme.surfaceBright,
        )
    )
}

@Composable
private fun LoginButton(
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    colors: ButtonColors,
    onLoginButtonClick: () -> Unit = {},
    content: @Composable (RowScope.() -> Unit)
) {
    Button(
        onClick = onLoginButtonClick,
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp),
        enabled = enabled,
        shape = RoundedCornerShape(16.dp),
        colors = colors
    ) {
        content()
    }
}

private fun UiText.asString(context: Context): String = when (this) {
    is UiText.Dynamic -> value
    is UiText.Res -> context.getString(id)
}