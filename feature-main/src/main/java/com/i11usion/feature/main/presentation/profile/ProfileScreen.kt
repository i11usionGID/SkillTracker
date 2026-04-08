package com.i11usion.feature.main.presentation.profile

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.i11usion.core.domain.model.user.Role
import com.i11usion.core.ui.di.LocalViewModelFactory
import com.i11usion.core.ui.helpers.asString
import com.i11usion.core.ui.template.ErrorScreen
import com.i11usion.feature.main.R
import com.i11usion.feature.main.presentation.user.list.model.UserUiModel
import kotlinx.coroutines.delay

@Composable
fun ProfileScreen(
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit,
    onLogoutNavigate: () -> Unit
) {
    val viewModel: ProfileViewModel = viewModel(factory = LocalViewModelFactory.current)
    val uiState = viewModel.state.collectAsState()
    val state = uiState.value

    val context = LocalContext.current

    var userToDelete by remember { mutableStateOf<UserUiModel?>(null) }
    var updatePasswordDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                ProfileEvent.AccountDeleted -> onLogoutNavigate()
                ProfileEvent.PasswordUpdated -> updatePasswordDialog = false
            }
        }
    }

    when (state) {
        is ProfileUiState.Content -> {

            val fsError = state.fullScreenError
            if (fsError != null) {
                ErrorScreen(
                    modifier = modifier.fillMaxSize(),
                    iconResId = fsError.iconResId,
                    cdResId = fsError.cdResId,
                    errorText = fsError.text.asString(context),
                    onRefreshClick = {
                        viewModel.processCommand(ProfileCommand.Refresh)
                    },
                    onBackClick = onBackClick
                )
                return
            }

            Column(
                modifier = modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = stringResource(R.string.profile),
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(16.dp))

                UserCard(
                    userName = state.user.username,
                    name = state.user.name,
                    shortName = state.user.shortName,
                    createdAt = state.user.createdAt,
                    role = state.user.role,
                    onDeleteUserClick = { userToDelete = state.user },
                    onUpdatePasswordClick = { updatePasswordDialog = true }
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            if (userToDelete != null) {
                AlertDialog(
                    onDismissRequest = { userToDelete = null },
                    title = {
                        Text(
                            text = stringResource(R.string.are_you_sure_want_delete_account),
                            fontSize = 24.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    },
                    text = {
                        Text(
                            text = stringResource(R.string.confirmation_delete_account),
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.surfaceVariant
                        )
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                val id = userToDelete!!.id
                                userToDelete = null
                                viewModel.processCommand(ProfileCommand.Delete(id))
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                        ) {
                            Text(
                                text = stringResource(R.string.delete),
                                fontSize = 16.sp,
                                color = Color.White
                            )
                        }
                    },
                    dismissButton = {
                        Button(
                            onClick = { userToDelete = null },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceBright)
                        ) {
                            Text(
                                text = stringResource(R.string.Cancel),
                                fontSize = 16.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    },
                    containerColor = MaterialTheme.colorScheme.onPrimary
                )
            }

            if (updatePasswordDialog) {
                ChangePasswordDialog(
                    passwordQuery = state.passwordQuery,
                    repeatPasswordQuery = state.repeatPasswordQuery,
                    createError = state.createError,
                    onPasswordQueryChange = {
                        viewModel.processCommand(ProfileCommand.InputPasswordQuery(it))
                    },
                    onRepeatPasswordQueryChange = {
                        viewModel.processCommand(ProfileCommand.InputRepeatPasswordQuery(it))
                    },
                    onDismiss = {
                        updatePasswordDialog = false
                        viewModel.processCommand(ProfileCommand.ClearPasswordInputs)
                    },
                    onSaveClick = {
                        viewModel.processCommand(ProfileCommand.UpdatePassword)
                    },
                    isLoading = state.isUpdatingPassword,
                    isSaveEnabled = state.canUpdate
                )
            }
        }

        is ProfileUiState.Error -> {
            ErrorScreen(
                modifier = modifier.fillMaxSize(),
                iconResId = state.model.iconResId,
                cdResId = state.model.cdResId,
                errorText = state.model.text.asString(context),
                onRefreshClick = {
                    viewModel.processCommand(ProfileCommand.Refresh)
                }
            )
        }

        ProfileUiState.Initial -> {}

        ProfileUiState.Loading -> {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator(
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun UserCard(
    modifier: Modifier = Modifier,
    userName: String,
    name: String,
    shortName: String,
    createdAt: String,
    role: Role,
    onDeleteUserClick: () -> Unit,
    onUpdatePasswordClick: () -> Unit
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.onPrimary,
            disabledContainerColor = MaterialTheme.colorScheme.onPrimary
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 1.dp,
            focusedElevation = 2.dp,
            pressedElevation = 2.dp
        ),
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.surfaceBright.copy(alpha = 0.3f)
        )
    ) {
        Column(modifier = Modifier.padding(vertical = 24.dp, horizontal = 16.dp)) {

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .background(
                            color = MaterialTheme.colorScheme.tertiaryContainer,
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = shortName,
                        color = MaterialTheme.colorScheme.tertiary,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 24.sp
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column {
                    Text(
                        text = name,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.SemiBold
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "@$userName",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 18.sp,
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = when (role) {
                    Role.MANAGER -> {
                        stringResource(R.string.status_manager)
                    }

                    Role.EMPLOYEE -> {
                        stringResource(R.string.status_employee)
                    }
                },
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 18.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = stringResource(R.string.created_at_with_colon) + createdAt,
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 18.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors()
                    .copy(containerColor = MaterialTheme.colorScheme.surfaceBright),
                shape = RoundedCornerShape(32.dp),
                onClick = onUpdatePasswordClick
            ) {
                Text(
                    text = stringResource(R.string.change_password),
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors()
                    .copy(containerColor = MaterialTheme.colorScheme.onError),
                shape = RoundedCornerShape(32.dp),
                onClick = onDeleteUserClick
            ) {
                Text(
                    text = stringResource(R.string.delete_account),
                    fontSize = 16.sp,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
private fun ChangePasswordDialog(
    passwordQuery: String,
    repeatPasswordQuery: String,
    createError: String?,
    onPasswordQueryChange: (String) -> Unit,
    onRepeatPasswordQueryChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onSaveClick: () -> Unit,
    isLoading: Boolean,
    isSaveEnabled: Boolean
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = stringResource(R.string.change_password),
                    fontSize = 24.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        },
        text = {
            Column {
                Text(
                    text = stringResource(R.string.password),
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(8.dp))

                InputField(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    query = passwordQuery,
                    onQueryChanged = onPasswordQueryChange,
                    isPassword = true
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = stringResource(R.string.repeat_password),
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(8.dp))

                InputField(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    query = repeatPasswordQuery,
                    onQueryChanged = onRepeatPasswordQueryChange,
                    isPassword = true
                )

                Spacer(modifier = Modifier.height(16.dp))

                if (createError != null) {
                    Text(
                        text = createError,
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 14.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        },
        confirmButton = {
            Button(
                modifier = Modifier.widthIn(min = 120.dp),
                enabled = isSaveEnabled && !isLoading,
                onClick = onSaveClick,
                colors = ButtonDefaults.buttonColors().copy(
                    containerColor = MaterialTheme.colorScheme.primary,
                    disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                    disabledContentColor = Color.White.copy(alpha = 0.3f)
                ),
            ) {
                val loadingText = rememberAnimatedLoadingText()

                Box(
                    modifier = Modifier.widthIn(min = 88.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (isLoading) loadingText else stringResource(R.string.save),
                        fontSize = 16.sp,
                        color = Color.White
                    )
                }
            }
        },
        dismissButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceBright
                )
            ) {
                Text(
                    text = stringResource(R.string.Cancel),
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.onPrimary
    )
}

@Composable
private fun InputField(
    modifier: Modifier = Modifier,
    query: String,
    onQueryChanged: (String) -> Unit,
    isPassword: Boolean = false
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    var passwordVisible by remember { mutableStateOf(false) }

    OutlinedTextField(
        value = query,
        onValueChange = onQueryChanged,
        placeholder = {
            if (!isFocused) {
                Text(
                    text = "••••••••",
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.surfaceBright
                )
            }
        },
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        interactionSource = interactionSource,
        visualTransformation = when {
            isPassword && !passwordVisible -> PasswordVisualTransformation()
            else -> VisualTransformation.None
        },
        trailingIcon = {
            if (isPassword) {
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(
                        imageVector = if (passwordVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.surfaceVariant
                    )
                }
            }
        },
        colors = OutlinedTextFieldDefaults.colors(
            unfocusedBorderColor = MaterialTheme.colorScheme.surfaceBright,
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedPlaceholderColor = MaterialTheme.colorScheme.surfaceBright,
        )
    )
}

@Composable
private fun rememberAnimatedLoadingText(
    baseText: String = "Загрузка"
): String {
    var dots by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        while (true) {
            dots = "."
            delay(500)

            dots = ".."
            delay(500)

            dots = "..."
            delay(500)

            dots = ""
            delay(500)
        }
    }

    return baseText + dots
}