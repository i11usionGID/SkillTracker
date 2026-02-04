package com.i11usion.feature.main.presentation.user

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.i11usion.core.domain.model.Role
import com.i11usion.core.ui.di.LocalViewModelFactory
import com.i11usion.feature.main.R
import com.i11usion.feature.main.presentation.user.model.UserUiModel

@Composable
fun UserScreen(
    modifier: Modifier = Modifier,
    onCreateUserClick: () -> Unit
) {
    val viewModel: UserViewModel = viewModel(factory = LocalViewModelFactory.current)
    val uiState = viewModel.state.collectAsState()
    val state = uiState.value

    when (state) {
        UserUiState.Initial -> {}
        UserUiState.Loading -> {
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

        is UserUiState.Content -> {
            var userToDelete by remember { mutableStateOf<UserUiModel?>(null) }

            LazyColumn(
                modifier = modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
            ) {
                item {
                    UserHeader(
                        onCreateUserClick = {
                            onCreateUserClick
                        }
                    )
                }

                item {
                    FilterCard(
                        query = state.userFilters.query,
                        selectedRole = state.userFilters.role,
                        onQueryChanged = { viewModel.processCommand(UserCommand.InputSearchQuery(it)) },
                        onRoleChanged = { viewModel.processCommand(UserCommand.SelectRole(it)) },
                    )

                    Spacer(modifier = Modifier.height(32.dp))
                }

                if (state.isListLoading) {
                    item {
                        Spacer(modifier = Modifier.height(64.dp))
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
                } else if (state.userList.isEmpty()) {
                    item {
                        Spacer(modifier = Modifier.height(32.dp))
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.ic_user),
                                contentDescription = stringResource(R.string.cd_user_not_found),
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.surfaceTint
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = stringResource(R.string.user_not_found),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                } else {
                    items(
                        items = state.userList,
                        key = { user -> user.id }
                    ) { user ->
                        val roleColor: Color
                        val roleContainerColor: Color
                        when (user.role) {
                            Role.MANAGER -> {
                                roleColor = MaterialTheme.colorScheme.tertiary
                                roleContainerColor = MaterialTheme.colorScheme.tertiaryContainer
                            }

                            Role.EMPLOYEE -> {
                                roleColor = MaterialTheme.colorScheme.surfaceVariant
                                roleContainerColor = MaterialTheme.colorScheme.surfaceBright
                            }
                        }
                        UserCard(
                            userName = user.username,
                            name = user.name,
                            shortName = user.shortName,
                            createdAt = user.createdAt,
                            roleText = user.roleText,
                            roleColor = roleColor,
                            roleContainerColor = roleContainerColor,
                            onDeleteUserClick = { userToDelete = user },
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }

            if (userToDelete != null) {
                AlertDialog(
                    onDismissRequest = { userToDelete = null },
                    title = { Text(text = stringResource(R.string.delete_user_question)) },
                    text = {
                        Text(
                            text = stringResource(R.string.confirmation_delete_user) + "${userToDelete!!.name}?"
                        )
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                val id = userToDelete!!.id
                                userToDelete = null
                                viewModel.processCommand(UserCommand.DeleteUser(id))
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                        ) {
                            Text(stringResource(R.string.delete))
                        }
                    },
                    dismissButton = {
                        Button(
                            onClick = { userToDelete = null },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Text(stringResource(R.string.Cancel))
                        }
                    },
                    containerColor = MaterialTheme.colorScheme.onPrimary
                )
            }

        }

        is UserUiState.Error -> {

        }
    }
}


@Composable
private fun UserHeader(
    onCreateUserClick: () -> Unit
) {
    Spacer(modifier = Modifier.height(16.dp))

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(R.string.users),
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = stringResource(R.string.manage_team),
            fontSize = 18.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(16.dp))

        Button(
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors()
                .copy(containerColor = MaterialTheme.colorScheme.primary),
            shape = RoundedCornerShape(16.dp),
            onClick = onCreateUserClick
        ) {
            Text(
                text = stringResource(R.string.add_user),
                fontSize = 18.sp,
                color = MaterialTheme.colorScheme.onPrimary
            )
        }
    }

    Spacer(modifier = Modifier.height(16.dp))
}

@Composable
private fun FilterCard(
    modifier: Modifier = Modifier,
    query: String,
    selectedRole: Role?,
    onQueryChanged: (String) -> Unit,
    onRoleChanged: (Role?) -> Unit
) {
    Card(
        modifier = modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.onPrimary
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.surfaceBright.copy(alpha = 0.3f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = stringResource(R.string.search),
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))

            val interactionSource = remember { MutableInteractionSource() }
            val isFocused by interactionSource.collectIsFocusedAsState()

            SearchField(
                interactionSource = interactionSource,
                query = query,
                onQueryChanged = onQueryChanged,
                isFocused = isFocused
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = stringResource(R.string.role),
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))

            val roleOptions = roleOptions()
            val selectedRoleTitle =
                roleOptions.firstOrNull { it.value == selectedRole }?.title
                    ?: roleOptions.first().title

            DropdownField(
                options = roleOptions.map { it.title },
                selected = selectedRoleTitle,
                onSelectedChange = { title ->
                    val role = roleOptions.first { it.title == title }.value
                    onRoleChanged(role)
                }
            )
        }
    }
}


@Composable
private fun SearchField(
    interactionSource: MutableInteractionSource,
    query: String,
    onQueryChanged: (String) -> Unit,
    isFocused: Boolean,
) {
    OutlinedTextField(
        modifier = Modifier.fillMaxWidth(),
        value = query,
        onValueChange = onQueryChanged,
        interactionSource = interactionSource,
        shape = RoundedCornerShape(20.dp),
        placeholder = {
            if (!isFocused) {
                Text(
                    stringResource(R.string.search_text_user),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    fontSize = 16.sp
                )
            }
        },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = stringResource(R.string.cd_search),
                modifier = Modifier.size(24.dp),
                tint = MaterialTheme.colorScheme.surfaceVariant
            )
        },
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.onPrimary,
            unfocusedContainerColor = MaterialTheme.colorScheme.onPrimary,
            unfocusedBorderColor = MaterialTheme.colorScheme.surfaceBright,
            focusedBorderColor = MaterialTheme.colorScheme.surfaceBright,
        )
    )
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DropdownField(
    modifier: Modifier = Modifier,
    options: List<String>,
    selected: String,
    onSelectedChange: (String) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = selected,
            onValueChange = {},
            readOnly = true,
            singleLine = true,
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth(),
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
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

@Composable
private fun UserCard(
    modifier: Modifier = Modifier,
    userName: String,
    name: String,
    shortName: String,
    createdAt: String,
    roleText: String,
    roleColor: Color,
    roleContainerColor: Color,
    onDeleteUserClick: () -> Unit
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
        Column(modifier = Modifier.padding(vertical = 24.dp, horizontal = 24.dp)) {

            Row {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .background(
                            color = roleContainerColor,
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = shortName,
                        color = roleColor,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 24.sp
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
                Box(
                    modifier = Modifier.background(
                        color = roleContainerColor,
                        shape = RoundedCornerShape(50.dp)
                    ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        text = roleText,
                        color = roleColor,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

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
                fontSize = 16.sp,
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = stringResource(R.string.created_at) + createdAt,
                color = MaterialTheme.colorScheme.surfaceVariant,
                fontSize = 16.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors()
                    .copy(containerColor = MaterialTheme.colorScheme.onError),
                shape = RoundedCornerShape(32.dp),
                onClick = onDeleteUserClick
            ) {
                Text(
                    text = stringResource(R.string.delete),
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    }
}


private data class RoleOption(val title: String, val value: Role?)

private fun roleOptions(): List<RoleOption> = listOf(
    RoleOption("Все роли", null),
    RoleOption("Менеджеры", Role.MANAGER),
    RoleOption("Сотрудники", Role.EMPLOYEE)
)