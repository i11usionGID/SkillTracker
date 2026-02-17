package com.i11usion.feature.main.presentation.quiz.detail

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.i11usion.core.domain.model.quiz.QuizAnalytics
import com.i11usion.core.domain.model.quiz.QuizStatus
import com.i11usion.core.ui.di.LocalViewModelFactory
import com.i11usion.feature.main.R
import com.i11usion.feature.main.presentation.quiz.detail.model.SectionQuestionsUiModel

@Composable
fun QuizDetailScreen(
    modifier: Modifier = Modifier,
    quizId: Int,
    onBack: () -> Unit,
    onCreateSectionClick: (Int) -> Unit,
    onUpdateQuizClick: (Int) -> Unit,
    onCreateAssignmentClick: (Int) -> Unit,
    onEditSectionClick: (Int) -> Unit,
) {
    val viewModel: QuizDetailViewModel = viewModel(factory = LocalViewModelFactory.current)
    val uiState = viewModel.state.collectAsState()
    val state = uiState.value

    LaunchedEffect(quizId) {
        viewModel.processCommand(QuizDetailCommand.Init(quizId))
    }

    when (state) {
        QuizDetailUiState.Initial -> {}
        QuizDetailUiState.Loading -> {
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

        is QuizDetailUiState.Content -> {
            LazyColumn(
                modifier = modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
            ) {
                item {
                    val statusText: String
                    val statusColor: Color
                    val statusContainerColor: Color

                    when (state.quiz.status) {
                        QuizStatus.DRAFT -> {
                            statusText = stringResource(R.string.draft)
                            statusColor = MaterialTheme.colorScheme.surfaceVariant
                            statusContainerColor = MaterialTheme.colorScheme.surfaceBright
                        }

                        QuizStatus.PUBLISHED -> {
                            statusText = stringResource(R.string.published)
                            statusColor = MaterialTheme.colorScheme.tertiary
                            statusContainerColor = MaterialTheme.colorScheme.tertiaryContainer
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    QuizDetailHeader(
                        statusText = statusText,
                        statusColor = statusColor,
                        statusContainerColor = statusContainerColor,
                        onBack = onBack,
                        onUpdateQuizClick = {
                            onUpdateQuizClick(quizId)
                        }
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                }

                item {
                    QuizDetailCard(
                        title = state.quiz.title,
                        description = state.quiz.description,
                        passPercent = state.quiz.passPercent,
                        createdAt = state.quiz.createdAt,
                        timeLimit = state.quiz.timeLimitSeconds,
                        maxAttempts = state.quiz.maxAttempts
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                }

                item {
                    FilterBlock(
                        selectedTab = state.selectedTab,
                        onSelect = { tab ->
                            viewModel.processCommand(QuizDetailCommand.SelectTab(tab))
                        }
                    )

                    Box(
                        modifier = Modifier
                            .height(2.dp)
                            .fillMaxWidth()
                            .background(color = MaterialTheme.colorScheme.surfaceBright)
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                }


                when (state.selectedTab) {
                    QuizDetailTab.SECTIONS -> {
                        item {
                            Row(modifier = Modifier.fillMaxWidth()) {
                                Button(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors()
                                        .copy(containerColor = MaterialTheme.colorScheme.primary),
                                    shape = RoundedCornerShape(16.dp),
                                    onClick = { onCreateSectionClick(quizId) }
                                ) {
                                    Text(
                                        text = stringResource(R.string.add_section),
                                        fontSize = 16.sp,
                                        color = MaterialTheme.colorScheme.onPrimary
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))
                        }

                        items(
                            items = state.sections,
                            key = { section -> section.id }
                        ) { section ->
                            SectionCard(
                                title = section.title,
                                orderIndex = section.orderIndex,
                                pickCount = section.pickCount,
                                description = section.description,
                                questionList = section.questions,
                                onEditSectionClick = {
                                    onEditSectionClick(section.id)
                                },
                                onDeleteSectionClick = {
                                    //viewModel.processCommand(QuizDetailCommand.DeleteSection(section.id))
                                }
                            )

                            Spacer(modifier = Modifier.height(16.dp))
                        }
                    }

                    QuizDetailTab.ASSIGNMENTS -> {
                        item {
                            Row(modifier = Modifier.fillMaxWidth()) {
                                Button(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors()
                                        .copy(containerColor = MaterialTheme.colorScheme.primary),
                                    shape = RoundedCornerShape(16.dp),
                                    onClick = { onCreateAssignmentClick(quizId) }
                                ) {
                                    Text(
                                        text = stringResource(R.string.create_assignment),
                                        fontSize = 16.sp,
                                        color = MaterialTheme.colorScheme.onPrimary
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))
                        }

                        items(
                            items = state.assignments,
                            key = { assignment -> assignment.id }
                        ) { assignment ->
                            AssignmentCard(
                                required = assignment.required,
                                allowedUserUsername = assignment.allowedUserUsername,
                                allowedUserName = assignment.allowedUserName,
                                taskTitle = assignment.taskTitle,
                                dueAt = assignment.dueAt,
                                maxAttempts = assignment.maxAttempts,
                                cooldownHours = assignment.cooldownHours,
                                attemptsTaken = assignment.attemptsTaken,
                                remainingAttempts = assignment.remainingAttempts,
                                onDeleteAssignment = {
//                                    viewModel.processCommand(
//                                        QuizDetailCommand.DeleteAssignment(
//                                            assignment.id
//                                        )
//                                    )
                                }
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                    }

                    QuizDetailTab.ANALYTICS -> {
                        item {
                            AnalyticsBlock(
                                analytics = state.analytics
                            )
                        }
                    }
                }
            }
        }

        is QuizDetailUiState.Error -> {

        }
    }
}

@Composable
private fun QuizDetailHeader(
    modifier: Modifier = Modifier,
    statusText: String,
    statusColor: Color,
    statusContainerColor: Color,
    onBack: () -> Unit,
    onUpdateQuizClick: () -> Unit
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
            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
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

        Spacer(modifier = Modifier.width(16.dp))

        Button(
            onClick = onUpdateQuizClick,
            modifier = Modifier.height(32.dp),
            colors = ButtonDefaults.buttonColors()
                .copy(containerColor = MaterialTheme.colorScheme.surfaceBright),
            shape = RoundedCornerShape(32.dp),
            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_edit),
                    contentDescription = stringResource(R.string.cd_edit_quiz),
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.width(4.dp))

                Text(
                    text = stringResource(R.string.edit),
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
private fun QuizDetailCard(
    modifier: Modifier = Modifier,
    title: String,
    description: String?,
    passPercent: String,
    createdAt: String,
    timeLimit: String?,
    maxAttempts: String?
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
                text = title,
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold
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

            Row(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.passing_score),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        fontSize = 16.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = passPercent + "%",
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.created_at),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        fontSize = 16.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = createdAt,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 16.sp,
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (timeLimit != null || maxAttempts != null) {
                Row(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (timeLimit != null) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = stringResource(R.string.time_limit),
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                fontSize = 16.sp
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = timeLimit,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontSize = 16.sp,
                            )
                        }
                    }

                    if (maxAttempts != null) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = stringResource(R.string.max_attempts),
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                fontSize = 16.sp
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = maxAttempts,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun FilterBlock(
    modifier: Modifier = Modifier,
    selectedTab: QuizDetailTab,
    onSelect: (QuizDetailTab) -> Unit
) {
    Row(modifier = modifier.fillMaxWidth()) {
        QuizDetailTab.entries.forEach { tab ->
            FilterItem(
                modifier = Modifier.weight(1f),
                filterText = tab.title,
                isSelected = tab == selectedTab,
                onSelectItem = { onSelect(tab) }
            )
        }
    }
}

@Composable
private fun FilterItem(
    modifier: Modifier = Modifier,
    filterText: String,
    isSelected: Boolean,
    onSelectItem: () -> Unit
) {
    Column(
        modifier = modifier.clickable(onClick = onSelectItem),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = filterText,
            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
            fontSize = 16.sp,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Box(
            modifier = Modifier
                .height(2.dp)
                .fillMaxWidth()
                .background(
                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                )
        )
    }
}

@Composable
private fun SectionCard(
    modifier: Modifier = Modifier,
    title: String,
    orderIndex: Int,
    pickCount: Int?,
    description: String?,
    questionList: List<SectionQuestionsUiModel>,
    onEditSectionClick: () -> Unit,
    onDeleteSectionClick: () -> Unit
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
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    modifier = Modifier.weight(1f),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.width(8.dp))

                Icon(
                    painter = painterResource(R.drawable.ic_edit),
                    contentDescription = stringResource(R.string.cd_update_question),
                    modifier = Modifier
                        .size(20.dp)
                        .clickable(
                            onClick = onEditSectionClick
                        ),
                    tint = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.width(8.dp))

                Icon(
                    painter = painterResource(R.drawable.ic_trash),
                    contentDescription = stringResource(R.string.delete_question),
                    modifier = Modifier
                        .size(20.dp)
                        .clickable(
                            onClick = onDeleteSectionClick
                        ),
                    tint = MaterialTheme.colorScheme.onError
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier.background(
                        color = MaterialTheme.colorScheme.surfaceBright,
                        shape = RoundedCornerShape(50.dp)
                    ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        text = stringResource(R.string.order_index) + "$orderIndex",
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 16.sp
                    )
                }

                if (pickCount != null) {

                    Spacer(modifier = Modifier.width(8.dp))

                    Box(
                        modifier = Modifier.background(
                            color = MaterialTheme.colorScheme.surfaceBright,
                            shape = RoundedCornerShape(50.dp)
                        ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            text = stringResource(R.string.pick_count) + "$pickCount",
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 16.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (!description.isNullOrBlank()) {
                Text(
                    text = description,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(16.dp))
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(color = MaterialTheme.colorScheme.surfaceBright)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = stringResource(R.string.questions) + "(${questionList.size}):",
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn(
                modifier = modifier
                    .fillMaxSize()
                    .heightIn(max = 1000.dp)
            ) {
                itemsIndexed(
                    items = questionList,
                    key = { _, question -> question.id }
                ) { index, question ->

                    QuestionItem(
                        number = index + 1,
                        text = question.text,
                        type = question.type
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
private fun QuestionItem(
    modifier: Modifier = Modifier,
    number: Int,
    text: String,
    type: String
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "$number.",
            color = MaterialTheme.colorScheme.surfaceBright,
            fontSize = 16.sp
        )

        Spacer(modifier = Modifier.width(4.dp))

        Text(
            text = text,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f),
            fontSize = 16.sp
        )

        Spacer(modifier = Modifier.width(4.dp))

        Box(
            modifier = Modifier.background(
                color = MaterialTheme.colorScheme.surfaceBright,
                shape = RoundedCornerShape(50.dp)
            ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                text = type,
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 16.sp
            )
        }
    }
}

@Composable
private fun AssignmentCard(
    modifier: Modifier = Modifier,
    required: Boolean,
    allowedUserUsername: String,
    allowedUserName: String,
    taskTitle: String?,
    dueAt: String?,
    maxAttempts: Int?,
    cooldownHours: Int,
    attemptsTaken: Int,
    remainingAttempts: Int?,
    onDeleteAssignment: () -> Unit
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

            Row(modifier = Modifier.fillMaxWidth()) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(30.dp)
                            .background(
                                color = MaterialTheme.colorScheme.surfaceBright,
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = allowedUserName[0].toString(),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 18.sp
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Column {
                        Text(
                            text = allowedUserName,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "@$allowedUserUsername",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 14.sp,
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                if (required) {
                    Box(
                        modifier = Modifier.background(
                            color = MaterialTheme.colorScheme.errorContainer,
                            shape = RoundedCornerShape(50.dp)
                        ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            text = stringResource(R.string.required),
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 16.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(50))
                        .background(
                            color = MaterialTheme.colorScheme.onError,
                            shape = RoundedCornerShape(50)
                        )
                        .clickable {
                            onDeleteAssignment()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_trash),
                        contentDescription = stringResource(R.string.cd_delete_assignment),
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (!taskTitle.isNullOrBlank()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_folder),
                        contentDescription = stringResource(R.string.cd_assignment_attach_to_task),
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.surfaceBright
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = stringResource(R.string.task) + taskTitle,
                        color = MaterialTheme.colorScheme.surfaceBright,
                        fontSize = 16.sp
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(color = MaterialTheme.colorScheme.surfaceBright)
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (!dueAt.isNullOrBlank()) {
                Text(
                    text = buildAnnotatedString {
                        withStyle(
                            style = SpanStyle(color = MaterialTheme.colorScheme.surfaceBright)
                        ) {
                            append(stringResource(R.string.deadline_with_colon))
                        }

                        withStyle(
                            style = SpanStyle(color = MaterialTheme.colorScheme.onSurface)
                        ) {
                            append(dueAt)
                        }
                    },
                    fontSize = 16.sp
                )

                Spacer(modifier = Modifier.height(16.dp))
            }

            if (maxAttempts != null) {
                Text(
                    text = buildAnnotatedString {
                        withStyle(
                            style = SpanStyle(color = MaterialTheme.colorScheme.surfaceBright)
                        ) {
                            append(stringResource(R.string.max_attempts_with_colon))
                        }

                        withStyle(
                            style = SpanStyle(color = MaterialTheme.colorScheme.onSurface)
                        ) {
                            append(maxAttempts.toString())
                        }
                    },
                    fontSize = 16.sp
                )

                Spacer(modifier = Modifier.height(16.dp))
            }

            Text(
                text = buildAnnotatedString {
                    withStyle(
                        style = SpanStyle(color = MaterialTheme.colorScheme.surfaceBright)
                    ) {
                        append(stringResource(R.string.cooldown_hours))
                    }

                    withStyle(
                        style = SpanStyle(color = MaterialTheme.colorScheme.onSurface)
                    ) {
                        append(cooldownHours.toString())
                    }
                },
                fontSize = 16.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = buildAnnotatedString {
                    withStyle(
                        style = SpanStyle(color = MaterialTheme.colorScheme.surfaceBright)
                    ) {
                        append(stringResource(R.string.attempts_taken))
                    }

                    withStyle(
                        style = SpanStyle(color = MaterialTheme.colorScheme.onSurface)
                    ) {
                        append(attemptsTaken.toString())
                    }
                },
                fontSize = 16.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (remainingAttempts != null) {
                Text(
                    text = buildAnnotatedString {
                        withStyle(
                            style = SpanStyle(color = MaterialTheme.colorScheme.surfaceBright)
                        ) {
                            append(stringResource(R.string.remaining_attempts))
                        }

                        withStyle(
                            style = SpanStyle(color = MaterialTheme.colorScheme.onSurface)
                        ) {
                            append(remainingAttempts.toString())
                        }
                    },
                    fontSize = 16.sp
                )

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun AnalyticsBlock(
    analytics: QuizAnalytics
) {
    AnalyticCard(
        title = stringResource(R.string.total_assignments),
        value = analytics.totalAssignments.toString(),
        valueColor = MaterialTheme.colorScheme.onSurface
    )

    Spacer(modifier = Modifier.height(16.dp))

    AnalyticCard(
        title = stringResource(R.string.total_attempts),
        value = analytics.totalAttempts.toString(),
        valueColor = MaterialTheme.colorScheme.onSurface
    )

    Spacer(modifier = Modifier.height(16.dp))

    AnalyticCard(
        title = stringResource(R.string.completed),
        value = analytics.completedAttempts.toString(),
        valueColor = MaterialTheme.colorScheme.onSurface
    )

    Spacer(modifier = Modifier.height(16.dp))

    AnalyticCard(
        title = stringResource(R.string.pass_rate),
        value = analytics.passRate.toString() + "%",
        valueColor = MaterialTheme.colorScheme.onTertiary
    )

    Spacer(modifier = Modifier.height(16.dp))
}

@Composable
private fun AnalyticCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    valueColor: Color
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
                text = title,
                color = MaterialTheme.colorScheme.surfaceBright,
                fontSize = 16.sp
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = value,
                color = valueColor,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

enum class QuizDetailTab(val title: String) {
    SECTIONS("Секции и вопросы"),
    ASSIGNMENTS("Назначения"),
    ANALYTICS("Аналитика")
}

