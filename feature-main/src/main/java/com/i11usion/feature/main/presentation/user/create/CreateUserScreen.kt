package com.i11usion.feature.main.presentation.user.create

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.i11usion.core.domain.model.user.Role
import com.i11usion.core.ui.di.LocalViewModelFactory
import com.i11usion.feature.main.R

@Composable
fun CreateUserScreen(
    modifier: Modifier = Modifier,
    onBack: () -> Unit,
) {
    val viewModel: CreateUserViewModel = viewModel(factory = LocalViewModelFactory.current)
    val uiState = viewModel.state.collectAsState()
    val state = uiState.value

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            onBack()
        }
    }

    when (state) {
        CreateUserUiState.Initial -> {}
        CreateUserUiState.Loading -> {
            Column(
                modifier = modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator(
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        is CreateUserUiState.Content -> {
            Column(
                modifier = modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(R.string.create_user),
                        fontWeight = FontWeight.Bold,
                        fontSize = 28.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                CreateUserCard(
                    userNameQuery = state.userNameQuery,
                    nameQuery = state.nameQuery,
                    passwordQuery = state.passwordQuery,
                    selectedRole = state.selectedRole,
                    onUserNameQueryChanged = {
                        viewModel.processCommand(CreateUserCommand.ChangeUserNameQuery(it))
                    },
                    onNameQueryChanged = {
                        viewModel.processCommand(CreateUserCommand.ChangeNameQuery(it))
                    },
                    onPasswordQueryChanged = {
                        viewModel.processCommand(CreateUserCommand.ChangePasswordQuery(it))
                    },
                    onRoleChanged = {
                        viewModel.processCommand(CreateUserCommand.ChangeUserRoleQuery(it))
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Button(
                        modifier = Modifier
                            .weight(1f)
                            .height(64.dp),
                        onClick = {
                            onBack()
                        },
                        colors = ButtonDefaults.buttonColors()
                            .copy(containerColor = MaterialTheme.colorScheme.surfaceBright),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.cancel),
                            fontSize = 18.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        modifier = Modifier
                            .weight(1f)
                            .height(64.dp),
                        onClick = {
                            viewModel.processCommand(CreateUserCommand.CreateUser)
                        },
                        enabled = state.canCreate && !state.isCreating,
                        colors = ButtonDefaults.buttonColors()
                            .copy(
                                containerColor = MaterialTheme.colorScheme.primary,
                                disabledContainerColor = MaterialTheme.colorScheme.primary.copy(
                                    alpha = 0.3f
                                ),
                                disabledContentColor = Color.White.copy(alpha = 0.3f)
                            ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        if (!state.isCreating) {
                            Text(
                                text = stringResource(R.string.create),
                                fontSize = 18.sp,
                                color = Color.White
                            )
                        }

                        if (state.isCreating) {
                            CircularProgressIndicator(
                                strokeWidth = 2.dp,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }

        is CreateUserUiState.Error -> {

        }
    }
}

@Composable
private fun CreateUserCard(
    modifier: Modifier = Modifier,
    userNameQuery: String,
    nameQuery: String,
    passwordQuery: String,
    selectedRole: Role,
    onUserNameQueryChanged: (String) -> Unit,
    onNameQueryChanged: (String) -> Unit,
    onPasswordQueryChanged: (String) -> Unit,
    onRoleChanged: (Role) -> Unit
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
            Text(
                text = stringResource(R.string.username),
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(8.dp))

            InputField(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                query = userNameQuery,
                placeHolderText = stringResource(R.string.ph_username),
                onQueryChanged = onUserNameQueryChanged
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = stringResource(R.string.name),
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(8.dp))

            InputField(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                query = nameQuery,
                placeHolderText = stringResource(R.string.sample_name),
                onQueryChanged = onNameQueryChanged
            )

            Spacer(modifier = Modifier.height(16.dp))

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
                placeHolderText = "••••••••",
                onQueryChanged = onPasswordQueryChanged,
                isPassword = true
            )


            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = stringResource(R.string.status),
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(8.dp))

            val roleOptions = roleOptions()

            val selectedRoleTitle =
                roleOptions.first { it.value == selectedRole }.title

            DropdownField(
                options = roleOptions.map { it.title },
                selected = selectedRoleTitle,
                placeholder = "",
                onSelectedChange = { title ->
                    val status = roleOptions.first { it.title == title }.value
                    onRoleChanged(status)
                }
            )
        }
    }
}

@Composable
private fun InputField(
    modifier: Modifier = Modifier,
    query: String,
    placeHolderText: String,
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
                    text = placeHolderText,
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DropdownField(
    modifier: Modifier = Modifier,
    options: List<String>,
    selected: String?,
    placeholder: String,
    onSelectedChange: (String) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = selected ?: "",
            onValueChange = {},
            readOnly = true,
            singleLine = true,
            placeholder = {
                Text(text = placeholder)
            },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth(),
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded)
            },
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.onPrimary,
                unfocusedContainerColor = MaterialTheme.colorScheme.onPrimary,
                unfocusedBorderColor = MaterialTheme.colorScheme.surfaceBright,
                focusedBorderColor = MaterialTheme.colorScheme.surfaceBright,
            )
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            containerColor = MaterialTheme.colorScheme.onPrimary
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onSelectedChange(option)
                        expanded = false
                    },
                    leadingIcon = {
                        if (option == selected) {
                            Icon(Icons.Default.Check, contentDescription = null)
                        }
                    }
                )
            }
        }
    }
}

private data class RoleOption(val title: String, val value: Role)

private fun roleOptions(): List<RoleOption> = listOf(
    RoleOption("Сотрудник", Role.EMPLOYEE),
    RoleOption("Менеджер", Role.MANAGER)
)