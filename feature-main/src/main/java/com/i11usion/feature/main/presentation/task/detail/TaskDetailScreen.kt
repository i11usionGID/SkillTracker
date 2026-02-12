package com.i11usion.feature.main.presentation.task.detail

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.i11usion.core.domain.model.user.Role
import com.i11usion.core.ui.di.LocalViewModelFactory
import com.i11usion.feature.main.R
import com.i11usion.feature.main.presentation.dashboard.model.TaskUiStatus
import com.i11usion.feature.main.presentation.task.detail.model.AttachmentUiModel
import com.i11usion.feature.main.presentation.task.detail.model.CommentUiModel
import com.i11usion.feature.main.presentation.task.detail.model.EditableComment

@Composable
fun TaskDetailScreen(
    modifier: Modifier = Modifier,
    taskId: Int,
    onBack: () -> Unit
) {
    val viewModel: TaskDetailViewModel = viewModel(factory = LocalViewModelFactory.current)
    val uiState = viewModel.state.collectAsState()
    val state = uiState.value

    val pickFileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            viewModel.processCommand(
                TaskDetailCommand.UploadAttachment(
                    taskId = taskId,
                    uri = uri
                )
            )
        }
    }

    LaunchedEffect(taskId) {
        viewModel.processCommand(TaskDetailCommand.Init(taskId))
    }

    when (state) {
        TaskDetailUiState.Initial -> {}
        TaskDetailUiState.Loading -> {
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

        is TaskDetailUiState.Content -> {
            var showProgressDialog by remember { mutableStateOf(false) }
            var progress by rememberSaveable(taskId) { mutableIntStateOf(state.task.progress.toInt()) }
            val statusColor: Color
            val statusContainerColor: Color
            when (state.task.status) {
                TaskUiStatus.PENDING -> {
                    statusColor = MaterialTheme.colorScheme.surfaceVariant
                    statusContainerColor = MaterialTheme.colorScheme.surfaceBright
                }

                TaskUiStatus.IN_PROGRESS -> {
                    statusColor = MaterialTheme.colorScheme.secondary
                    statusContainerColor = MaterialTheme.colorScheme.secondaryContainer
                }

                TaskUiStatus.DONE -> {
                    statusColor = MaterialTheme.colorScheme.tertiary
                    statusContainerColor = MaterialTheme.colorScheme.tertiaryContainer
                }
            }

            LazyColumn(
                modifier = modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
            ) {
                item {
                    Spacer(modifier = Modifier.height(16.dp))

                    TaskDetailHeader(
                        userRole = state.user.role,
                        statusText = state.task.statusText,
                        statusColor = statusColor,
                        statusContainerColor = statusContainerColor,
                        onBack = onBack,
                        onDeleteTaskClick = {
                            viewModel.processCommand(
                                TaskDetailCommand.DeleteTask(
                                    taskId
                                )
                            )
                        }
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                }

                item {
                    TaskDetailCard(
                        title = state.task.title,
                        description = state.task.description,
                        userRole = state.user.role,
                        name = state.task.employee.name,
                        userName = state.task.employee.username,
                        shortName = state.task.employee.shortName,
                        deadline = state.task.deadline,
                        progress = state.task.progress,
                        onUpdateProgressClick = {
                            showProgressDialog = true
                        }
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                }

                item {
                    if (showProgressDialog) {
                        UpdateProgressDialog(
                            progress = progress,
                            onProgressChange = {
                                progress = it
                            },
                            onDismiss = {
                                showProgressDialog = false
                            },
                            onSaveClick = {
                                viewModel.processCommand(
                                    TaskDetailCommand.UpdateProgress(
                                        newProgress = progress,
                                        taskId = state.task.id
                                    )
                                )
                                showProgressDialog = false
                            }
                        )
                    }
                }

                item {
                    AttachmentsCard(
                        attachmentsList = state.attachments,
                        userId = state.user.id,
                        onUploadFileClick = {
                            pickFileLauncher.launch(arrayOf("*/*"))
                        },
                        onDeleteAttachmentClick = { attachmentId ->
                            viewModel.processCommand(TaskDetailCommand.DeleteAttachment(attachmentId))
                        },
                        onDownloadAttachmentClick = { TODO() }
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                }

                item {
                    CommentsCard(
                        userId = state.user.id,
                        isCommentsLoading = state.comments.isLoading,
                        query = state.comments.query,
                        commentList = state.comments.commentList,
                        editableComment = state.comments.editableComment,
                        onQueryChanged = {
                            viewModel.processCommand(
                                TaskDetailCommand.InputCommentQuery(
                                    it
                                )
                            )
                        },
                        onSendCommentClick = { viewModel.processCommand(TaskDetailCommand.AddComment) },
                        onStartEditCommentClick = {
                            viewModel.processCommand(
                                TaskDetailCommand.StartEditComment(
                                    it
                                )
                            )
                        },
                        onDeleteCommentClick = {
                            viewModel.processCommand(
                                TaskDetailCommand.DeleteComment(
                                    it
                                )
                            )
                        },
                        onEditTextChanged = {
                            viewModel.processCommand(
                                TaskDetailCommand.EditCommentTextChanged(
                                    it
                                )
                            )
                        },
                        onSaveEditClick = { viewModel.processCommand(TaskDetailCommand.FinishEditComment) },
                        onCancelEditClick = { viewModel.processCommand(TaskDetailCommand.CancelEditComment) }
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }

        is TaskDetailUiState.Error -> {
            Text(
                text = state.message
            )
        }
    }

}

@Composable
private fun TaskDetailHeader(
    modifier: Modifier = Modifier,
    userRole: Role,
    statusText: String,
    statusColor: Color,
    statusContainerColor: Color,
    onBack: () -> Unit,
    onDeleteTaskClick: () -> Unit

) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Button(
            onClick = onBack,
            modifier = Modifier.height(32.dp),
            colors = ButtonDefaults.buttonColors()
                .copy(containerColor = MaterialTheme.colorScheme.surfaceBright),
            shape = RoundedCornerShape(32.dp),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                    contentDescription = stringResource(R.string.cd_back),
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = stringResource(R.string.back),
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        Spacer(modifier = Modifier.width(16.dp))

        Box(
            modifier = Modifier.background(
                color = statusContainerColor,
                shape = RoundedCornerShape(50.dp)
            ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                text = statusText,
                color = statusColor,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )
        }

        if (userRole == Role.MANAGER) {
            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = onDeleteTaskClick,
                modifier = Modifier.height(32.dp),
                colors = ButtonDefaults.buttonColors()
                    .copy(containerColor = MaterialTheme.colorScheme.onError),
                shape = RoundedCornerShape(32.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_trash),
                        contentDescription = stringResource(R.string.cd_delete_task),
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        }
    }
}

@Composable
private fun TaskDetailCard(
    modifier: Modifier = Modifier,
    title: String,
    description: String?,
    userRole: Role,
    name: String,
    userName: String,
    shortName: String,
    deadline: String?,
    progress: String,
    onUpdateProgressClick: () -> Unit
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

            Text(
                text = title,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (!description.isNullOrBlank()) {
                Text(
                    text = description,
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    fontSize = 16.sp
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            if (userRole == Role.MANAGER) {
                Text(
                    text = stringResource(R.string.worker),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    fontSize = 16.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .background(
                                color = MaterialTheme.colorScheme.surfaceBright,
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = shortName,
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 24.sp
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Column {
                        Text(
                            text = name,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "@$userName",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 16.sp,
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }

            if (!deadline.isNullOrBlank()) {
                Text(
                    text = stringResource(R.string.deadline),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    fontSize = 16.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = deadline,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 16.sp
                )

                Spacer(modifier = Modifier.height(16.dp))
            }

            ProgressBlock(
                progress = progress,
                onUpdateProgressClick = onUpdateProgressClick,
            )
        }
    }
}

@Composable
private fun ProgressBlock(
    modifier: Modifier = Modifier,
    progress: String,
    onUpdateProgressClick: () -> Unit
) {
    Column(
        modifier = modifier.fillMaxSize()
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.progress),
                color = MaterialTheme.colorScheme.surfaceVariant,
                fontSize = 16.sp
            )

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = onUpdateProgressClick,
                modifier = Modifier.height(32.dp),
                colors = ButtonDefaults.buttonColors()
                    .copy(containerColor = MaterialTheme.colorScheme.surfaceBright),
                shape = RoundedCornerShape(32.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp)
            ) {
                Text(
                    text = stringResource(R.string.update_progress),
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 14.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        LineProgress(progress = progress)

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = progress + stringResource(R.string.percent_complited),
            color = MaterialTheme.colorScheme.surfaceVariant,
            fontSize = 14.sp
        )
    }
}

@Composable
private fun LineProgress(
    progress: String,
    modifier: Modifier = Modifier,
    height: Dp = 3.dp,
    corner: Dp = 50.dp,
) {
    val progressPercentFloat = (progress.toInt() / 100f).coerceIn(0f, 1f)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .clip(RoundedCornerShape(corner))
            .background(MaterialTheme.colorScheme.surfaceBright)
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(progressPercentFloat)
                .background(MaterialTheme.colorScheme.primary)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun UpdateProgressDialog(
    progress: Int,
    onProgressChange: (Int) -> Unit,
    onDismiss: () -> Unit,
    onSaveClick: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(20.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.update_progress),
                    style = MaterialTheme.typography.titleLarge
                )

                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = null
                    )
                }
            }
        },
        text = {
            Column {
                Text(
                    text = stringResource(R.string.progress) + ": $progress%",
                    style = MaterialTheme.typography.bodyLarge
                )

                Spacer(Modifier.height(16.dp))

                Slider(
                    value = progress.toFloat(),
                    onValueChange = { onProgressChange(it.toInt()) },
                    modifier = Modifier.fillMaxWidth(),
                    valueRange = 0f..100f,
                    colors = SliderDefaults.colors(
                        activeTrackColor = Color.Transparent,
                        inactiveTrackColor = Color.Transparent,
                        thumbColor = Color.Transparent,
                        activeTickColor = Color.Transparent,
                        inactiveTickColor = Color.Transparent
                    ),
                    thumb = {
                        Box(
                            modifier = Modifier
                                .size(30.dp)
                                .clip(CircleShape)
                                .background(
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    shape = CircleShape
                                )
                                .border(
                                    1.dp,
                                    MaterialTheme.colorScheme.surfaceVariant,
                                    CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(20.dp)
                                    .background(MaterialTheme.colorScheme.primary, CircleShape)
                            )
                        }
                    },
                    track = { _ ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(100))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(progress / 100f)
                                .height(6.dp)
                                .clip(RoundedCornerShape(100))
                                .background(MaterialTheme.colorScheme.primary)
                        )
                    }
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onSaveClick,
                modifier = Modifier.height(32.dp),
                colors = ButtonDefaults.buttonColors()
                    .copy(containerColor = MaterialTheme.colorScheme.primary),
                shape = RoundedCornerShape(32.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp)
            ) {
                Text(
                    text = stringResource(R.string.save),
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    )
}

@Composable
private fun AttachmentsCard(
    modifier: Modifier = Modifier,
    attachmentsList: List<AttachmentUiModel>,
    userId: Int,
    onUploadFileClick: () -> Unit,
    onDeleteAttachmentClick: (Int) -> Unit,
    onDownloadAttachmentClick: (Int) -> Unit
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
        Row(
            modifier = Modifier.padding(vertical = 24.dp, horizontal = 24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.attachment),
                fontSize = 24.sp,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = onUploadFileClick,
                modifier = Modifier.height(32.dp),
                colors = ButtonDefaults.buttonColors()
                    .copy(containerColor = MaterialTheme.colorScheme.surfaceBright),
                shape = RoundedCornerShape(32.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp)
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_attachment),
                    contentDescription = stringResource(R.string.cd_upload_attachment),
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = stringResource(R.string.upload_attachment),
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 14.sp
                )
            }
        }

        if (attachmentsList.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(88.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(R.string.no_attacments_yet),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    fontSize = 16.sp
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 500.dp)
            ) {
                items(
                    items = attachmentsList,
                    key = { attachments -> attachments.id }
                ) { attachment ->
                    AttachmentItem(
                        attachmentName = attachment.originalFileName,
                        createdAt = attachment.createdAt,
                        isAttachmentOwner = attachment.uploadedById == userId,
                        onDeleteAttachmentClick = {
                            onDeleteAttachmentClick(attachment.id)
                        },
                        onDownloadAttachmentClick = {
                            onDownloadAttachmentClick(attachment.id)
                        }
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
private fun AttachmentItem(
    modifier: Modifier = Modifier,
    attachmentName: String,
    createdAt: String,
    isAttachmentOwner: Boolean,
    onDeleteAttachmentClick: () -> Unit,
    onDownloadAttachmentClick: () -> Unit
) {
    Row(
        modifier = modifier.background(color = MaterialTheme.colorScheme.surface),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_list),
            contentDescription = stringResource(R.string.cd_attachment),
            tint = MaterialTheme.colorScheme.surfaceBright,
            modifier = Modifier.size(20.dp)
        )

        Spacer(modifier = Modifier.width(8.dp))

        Column {
            Text(
                text = attachmentName,
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 16.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = createdAt,
                color = MaterialTheme.colorScheme.surfaceVariant,
                fontSize = 14.sp
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        if (isAttachmentOwner) {
            Button(
                colors = ButtonDefaults.buttonColors()
                    .copy(containerColor = MaterialTheme.colorScheme.onError),
                shape = RoundedCornerShape(32.dp),
                onClick = onDeleteAttachmentClick
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_trash),
                    contentDescription = stringResource(R.string.cd_attachment),
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(8.dp))
        }

        Button(
            colors = ButtonDefaults.buttonColors()
                .copy(containerColor = MaterialTheme.colorScheme.surfaceBright),
            shape = RoundedCornerShape(32.dp),
            onClick = onDownloadAttachmentClick
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_download),
                contentDescription = stringResource(R.string.cd_attachment),
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun CommentsCard(
    modifier: Modifier = Modifier,
    userId: Int,
    isCommentsLoading: Boolean,
    query: String,
    commentList: List<CommentUiModel>,
    editableComment: EditableComment?,
    onQueryChanged: (String) -> Unit,
    onSendCommentClick: () -> Unit,
    onStartEditCommentClick: (Int) -> Unit,
    onDeleteCommentClick: (Int) -> Unit,
    onEditTextChanged: (String) -> Unit,
    onSaveEditClick: () -> Unit,
    onCancelEditClick: () -> Unit
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

            Text(
                text = stringResource(R.string.comments),
                fontSize = 24.sp,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            InputFieldComment(
                isLoading = isCommentsLoading,
                query = query,
                onQueryChanged = onQueryChanged
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onSendCommentClick,
                colors = ButtonDefaults.buttonColors()
                    .copy(containerColor = MaterialTheme.colorScheme.primary),
                modifier = Modifier.height(32.dp),
                shape = RoundedCornerShape(32.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                enabled = !isCommentsLoading
            ) {
                Text(
                    text = stringResource(R.string.send),
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontSize = 16.sp
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            val editingId = editableComment?.commentId
            val editingText = editableComment?.text.orEmpty()

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 500.dp)
            ) {
                items(commentList, key = { it.id }) { comment ->
                    val isEditing = comment.id == editingId

                    CommentItem(
                        commentOwnerName = comment.authorName,
                        createdAt = comment.createdAt,
                        text = comment.text,
                        isCommentOwner = comment.ownerId == userId,
                        isEditing = isEditing,
                        editingText = editingText,
                        onEditingTextChanged = onEditTextChanged,
                        onSaveEditClick = onSaveEditClick,
                        onCancelEditClick = onCancelEditClick,
                        onStartEditCommentClick = { onStartEditCommentClick(comment.id) },
                        onDeleteCommentClick = { onDeleteCommentClick(comment.id) }
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
private fun InputFieldComment(
    modifier: Modifier = Modifier,
    isLoading: Boolean,
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
                Text(
                    text = stringResource(R.string.add_comment),
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.surfaceBright
                )
            }
        },
        modifier = modifier
            .fillMaxWidth()
            .height(80.dp),
        shape = RoundedCornerShape(20.dp),
        interactionSource = interactionSource,
        colors = OutlinedTextFieldDefaults.colors(
            unfocusedBorderColor = MaterialTheme.colorScheme.surfaceBright,
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedPlaceholderColor = MaterialTheme.colorScheme.surfaceBright,
        )
    )
}

@Composable
private fun CommentItem(
    modifier: Modifier = Modifier,
    commentOwnerName: String,
    createdAt: String,
    text: String,
    isCommentOwner: Boolean,
    isEditing: Boolean,
    editingText: String,
    onEditingTextChanged: (String) -> Unit,
    onSaveEditClick: () -> Unit,
    onCancelEditClick: () -> Unit,
    onStartEditCommentClick: () -> Unit,
    onDeleteCommentClick: () -> Unit
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .width(4.dp)
                .background(color = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
        )

        Spacer(modifier = Modifier.width(8.dp))

        Column(Modifier.weight(1f)) {
            Text(
                text = commentOwnerName,
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 16.sp
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = createdAt,
                color = MaterialTheme.colorScheme.surfaceVariant,
                fontSize = 14.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (!isEditing) {
                Text(
                    text = text,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 16.sp
                )
            } else {
                OutlinedTextField(
                    value = editingText,
                    onValueChange = onEditingTextChanged,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)
                )

                Spacer(Modifier.height(12.dp))

                Row {
                    Button(
                        onClick = onSaveEditClick,
                        colors = ButtonDefaults.buttonColors()
                            .copy(containerColor = MaterialTheme.colorScheme.primary),
                        modifier = Modifier.height(32.dp),
                        shape = RoundedCornerShape(32.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                    ) {
                        Text(
                            stringResource(R.string.save),
                            color = MaterialTheme.colorScheme.onPrimary,
                            fontSize = 14.sp
                        )
                    }

                    Spacer(Modifier.width(8.dp))

                    Button(
                        onClick = onCancelEditClick,
                        colors = ButtonDefaults.buttonColors()
                            .copy(containerColor = MaterialTheme.colorScheme.surfaceBright),
                        modifier = Modifier.height(32.dp),
                        shape = RoundedCornerShape(32.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                    ) {
                        Text(
                            stringResource(R.string.cancel),
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }

        if (isCommentOwner && !isEditing) {
            Icon(
                painter = painterResource(R.drawable.ic_edit),
                contentDescription = stringResource(R.string.cd_edit_comment),
                modifier = Modifier
                    .size(16.dp)
                    .clickable(
                        onClick = onStartEditCommentClick
                    ),
                tint = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.width(4.dp))

            Icon(
                painter = painterResource(R.drawable.ic_trash),
                contentDescription = stringResource(R.string.cd_delete_comment),
                modifier = Modifier
                    .size(16.dp)
                    .clickable(
                        onClick = onDeleteCommentClick
                    ),
                tint = MaterialTheme.colorScheme.onError
            )
        }
    }
}