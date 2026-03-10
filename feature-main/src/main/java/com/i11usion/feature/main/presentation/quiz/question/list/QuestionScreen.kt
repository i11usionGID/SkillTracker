package com.i11usion.feature.main.presentation.quiz.question.list

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.i11usion.core.domain.model.quiz.QuestionOption
import com.i11usion.core.domain.model.quiz.QuestionType
import com.i11usion.core.ui.di.LocalViewModelFactory
import com.i11usion.core.ui.helpers.asString
import com.i11usion.core.ui.template.ErrorScreen
import com.i11usion.feature.main.R
import com.i11usion.feature.main.presentation.quiz.question.list.model.QuestionUiModel
import com.i11usion.feature.main.presentation.quiz.question.list.model.TextAnswerUiModel

@Composable
fun QuestionScreen(
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit,
    onCreateQuestionClick: () -> Unit,
    onUpdateQuestionClick: (Int) -> Unit
) {
    val viewModel: QuestionViewModel = viewModel(factory = LocalViewModelFactory.current)
    val uiState = viewModel.state.collectAsState()
    val state = uiState.value

    val context = LocalContext.current

    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner, viewModel) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.processCommand(QuestionCommand.Refresh)
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    when (state) {
        QuestionUiState.Initial -> {}

        is QuestionUiState.Content -> {
            var showDeleteQuestionDialog by remember { mutableStateOf<QuestionUiModel?>(null) }

            val fsError = state.fullScreenError
            if (fsError != null) {
                ErrorScreen(
                    modifier = modifier.fillMaxSize(),
                    iconResId = fsError.iconResId,
                    cdResId = fsError.cdResId,
                    errorText = fsError.text.asString(context),
                    onRefreshClick = {
                        viewModel.processCommand(QuestionCommand.Refresh)
                    },
                    onBackClick = onBackClick
                )
                return
            }

            LazyColumn(
                modifier = modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
            ) {
                item {
                    Spacer(modifier = Modifier.height(16.dp))

                    QuestionHeader(
                        onBackClick = onBackClick,
                        onCreateQuestionClick = onCreateQuestionClick
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(24.dp))

                    FilterBlock(
                        selected = state.selectedFilter,
                        onSelect = { viewModel.processCommand(QuestionCommand.SelectFilter(it)) }
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                }

                if (state.isLoading) {
                    item {
                        Spacer(modifier = Modifier.height(160.dp))

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
                } else {
                    items(
                        items = state.questionList,
                        key = { question -> question.id }
                    ) { question ->
                        QuestionCard(
                            text = question.text,
                            type = question.questionType,
                            typeText = question.questionTypeText,
                            questionOptions = question.options,
                            textAnswers = question.textAnswers,
                            explanation = question.explanation,
                            onEditQuestionClick = {
                                onUpdateQuestionClick(question.id)
                            },
                            onDeleteQuestionClick = { showDeleteQuestionDialog = question }
                        )

                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }

            if (showDeleteQuestionDialog != null) {
                AlertDialog(
                    onDismissRequest = { showDeleteQuestionDialog = null },
                    title = {
                        Text(
                            text = stringResource(R.string.delete_question_question),
                            fontSize = 24.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    },
                    text = {
                        Text(
                            text = stringResource(R.string.confirmation_delete_question),
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.surfaceVariant
                        )
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                val id = showDeleteQuestionDialog!!.id
                                showDeleteQuestionDialog = null
                                viewModel.processCommand(
                                    QuestionCommand.DeleteQuestion(
                                        id
                                    )
                                )
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
                            onClick = { showDeleteQuestionDialog = null },
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
        }


        is QuestionUiState.Error -> {
            ErrorScreen(
                modifier = modifier.fillMaxSize(),
                iconResId = state.model.iconResId,
                cdResId = state.model.cdResId,
                errorText = state.model.text.asString(context),
                onRefreshClick = {
                    viewModel.processCommand(QuestionCommand.Refresh)
                },
                onBackClick = onBackClick
            )
        }
    }
}

@Composable
private fun QuestionHeader(
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit,
    onCreateQuestionClick: () -> Unit
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(R.string.all_questions),
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = stringResource(R.string.create_and_manage_questions_for_quiz),
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.surfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )


        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onBackClick,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors()
                .copy(containerColor = MaterialTheme.colorScheme.surfaceBright),
            shape = RoundedCornerShape(32.dp)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Default.ArrowBack,
                contentDescription = stringResource(R.string.cd_back),
                modifier = Modifier.size(24.dp),
                tint = MaterialTheme.colorScheme.onSurface
            )

            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(R.string.back_to_tests),
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 18.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors()
                .copy(containerColor = MaterialTheme.colorScheme.primary),
            shape = RoundedCornerShape(32.dp),
            onClick = onCreateQuestionClick
        ) {
            Text(
                text = stringResource(R.string.create_question),
                fontSize = 18.sp,
                color = Color.White
            )
        }
    }
}

@Composable
private fun FilterBlock(
    modifier: Modifier = Modifier,
    selected: QuestionFilter,
    onSelect: (QuestionFilter) -> Unit
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
    ) {
        QuestionFilter.entries.forEachIndexed { index, filter ->
            FilterItem(
                text = filter.title,
                isSelect = filter == selected,
                onClick = { onSelect(filter) }
            )
            if (index != QuestionFilter.entries.lastIndex) {
                Spacer(modifier = Modifier.width(4.dp))
            }
        }
    }
}


@Composable
private fun FilterItem(
    text: String,
    isSelect: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .widthIn(min = 80.dp, max = 160.dp)
            .heightIn(min = 64.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(
                color = if (isSelect)
                    MaterialTheme.colorScheme.primary
                else
                    MaterialTheme.colorScheme.surfaceBright
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = if (isSelect)
                Color.White
            else
                MaterialTheme.colorScheme.onSurface,
            fontSize = 16.sp,
            textAlign = TextAlign.Center,
            maxLines = 2
        )
    }
}

@Composable
private fun QuestionCard(
    modifier: Modifier = Modifier,
    text: String,
    type: QuestionType,
    typeText: String,
    questionOptions: List<QuestionOption>,
    textAnswers: List<TextAnswerUiModel>,
    explanation: String?,
    onEditQuestionClick: () -> Unit,
    onDeleteQuestionClick: () -> Unit
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
            QuestionCardHeader(
                typeText = typeText,
                onEditQuestionClick = onEditQuestionClick,
                onDeleteQuestionClick = onDeleteQuestionClick
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = text,
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = if (type == QuestionType.TEXT) {
                    stringResource(R.string.right_questions)
                } else {
                    stringResource(R.string.answer_options)
                },
                color = MaterialTheme.colorScheme.surfaceVariant,
                fontSize = 14.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (type == QuestionType.TEXT) {
                textAnswers.forEach { textAnswer ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .widthIn(max = 160.dp)
                                .background(
                                    color = MaterialTheme.colorScheme.surfaceBright,
                                    shape = RoundedCornerShape(50.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                                text = textAnswer.matcherTypeText,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontSize = 14.sp,
                                textAlign = TextAlign.Center
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            Text(
                                text = textAnswer.value,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                }
            } else {
                questionOptions.forEach { option ->
                    val iconResId: Int
                    val optionColor: Color
                    val cdResId: Int
                    if (option.isCorrect) {
                        iconResId = R.drawable.ic_check_mark
                        optionColor = MaterialTheme.colorScheme.tertiary
                        cdResId = R.string.cd_right_answer
                    } else {
                        iconResId = R.drawable.ic_fail
                        optionColor = MaterialTheme.colorScheme.error
                        cdResId = R.string.cd_wrong_answer
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            painter = painterResource(iconResId),
                            contentDescription = stringResource(cdResId),
                            modifier = Modifier.size(16.dp),
                            tint = optionColor
                        )

                        Spacer(modifier = Modifier.width(4.dp))

                        Text(
                            text = option.text,
                            color = optionColor,
                            fontSize = 14.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                }
            }

            if (!explanation.isNullOrBlank()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(IntrinsicSize.Min)
                ) {
                    Box(
                        modifier = Modifier
                            .width(4.dp)
                            .fillMaxHeight()
                            .background(
                                color = MaterialTheme.colorScheme.secondary
                            )
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                color = MaterialTheme.colorScheme.secondaryContainer
                            ),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Text(
                            text = buildAnnotatedString {
                                withStyle(
                                    style = SpanStyle(fontWeight = FontWeight.Bold)
                                ) {
                                    append(stringResource(R.string.explanation))
                                }
                                append(explanation)
                            },
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.padding(4.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun QuestionCardHeader(
    modifier: Modifier = Modifier,
    typeText: String,
    onEditQuestionClick: () -> Unit,
    onDeleteQuestionClick: () -> Unit
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.background(
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = RoundedCornerShape(50.dp)
            ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                text = typeText,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        Icon(
            painter = painterResource(R.drawable.ic_edit),
            contentDescription = stringResource(R.string.cd_update_question),
            modifier = Modifier
                .size(20.dp)
                .clickable(
                    onClick = onEditQuestionClick
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
                    onClick = onDeleteQuestionClick
                ),
            tint = MaterialTheme.colorScheme.onError
        )
    }
}