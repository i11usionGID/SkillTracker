package com.i11usion.feature.main.presentation.quiz.question.create

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.i11usion.core.domain.model.quiz.MatcherType
import com.i11usion.core.domain.model.quiz.QuestionOption
import com.i11usion.core.domain.model.quiz.QuestionType
import com.i11usion.core.domain.model.quiz.TextAnswer
import com.i11usion.core.ui.di.LocalViewModelFactory
import com.i11usion.core.ui.helpers.asString
import com.i11usion.core.ui.template.ErrorScreen
import com.i11usion.feature.main.R

@Composable
fun CreateQuestionScreen(
    modifier: Modifier = Modifier,
    onCancelClick: () -> Unit
) {
    val viewModel: CreateQuestionViewModel = viewModel(factory = LocalViewModelFactory.current)
    val uiState = viewModel.state.collectAsState()
    val state = uiState.value

    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                CreateQuestionEvent.Created -> onCancelClick()
                CreateQuestionEvent.Close -> onCancelClick()
            }
        }
    }

    when (state) {
        CreateQuestionUiState.Initial -> {}
        is CreateQuestionUiState.Content -> {

            val fsError = state.fullScreenError
            if (fsError != null) {
                ErrorScreen(
                    modifier = modifier.fillMaxSize(),
                    iconResId = fsError.iconResId,
                    cdResId = fsError.cdResId,
                    errorText = fsError.text.asString(context),
                    onRefreshClick = {
                        viewModel.processCommand(CreateQuestionCommand.Refresh)
                    }
                )
                return
            }

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
                        text = stringResource(R.string.create_question),
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))


                CreateQuestionCard(
                    selectedType = state.questionType,
                    queryText = state.text,
                    explanation = state.explanation,
                    options = state.options,
                    textAnswers = state.textAnswers,
                    canCreate = state.canCreate,
                    isCreating = state.isCreating,

                    onTypeChanged = { viewModel.processCommand(CreateQuestionCommand.ChangeType(it)) },
                    onTextQueryChanged = {
                        viewModel.processCommand(
                            CreateQuestionCommand.ChangeText(
                                it
                            )
                        )
                    },
                    onExplanationQueryChanged = {
                        viewModel.processCommand(
                            CreateQuestionCommand.ChangeExplanation(
                                it
                            )
                        )
                    },
                    onAddAnswerClick = { viewModel.processCommand(CreateQuestionCommand.AddAnswerOrOption) },

                    onTextAnswerChanged = { id, value ->
                        viewModel.processCommand(
                            CreateQuestionCommand.ChangeTextAnswerValue(
                                id,
                                value
                            )
                        )
                    },
                    onTextAnswerMatcherSelected = { id, type ->
                        viewModel.processCommand(
                            CreateQuestionCommand.SetTextAnswerMatcher(
                                id,
                                type
                            )
                        )
                    },
                    onRemoveTextAnswer = { id ->
                        viewModel.processCommand(
                            CreateQuestionCommand.RemoveTextAnswer(
                                id
                            )
                        )
                    },

                    onOptionTextChanged = { id, value ->
                        viewModel.processCommand(
                            CreateQuestionCommand.ChangeOptionText(
                                id,
                                value
                            )
                        )
                    },
                    onToggleOptionCorrect = { id ->
                        viewModel.processCommand(
                            CreateQuestionCommand.ToggleOptionCorrect(
                                id
                            )
                        )
                    },
                    onRemoveOption = { id ->
                        viewModel.processCommand(
                            CreateQuestionCommand.RemoveOption(
                                id
                            )
                        )
                    },
                    onCancelClick = { viewModel.processCommand(CreateQuestionCommand.Cancel) },
                    onCreateTaskClick = { viewModel.processCommand(CreateQuestionCommand.Create) }
                )
            }
        }

        is CreateQuestionUiState.Error -> {
            ErrorScreen(
                modifier = modifier.fillMaxSize(),
                iconResId = state.model.iconResId,
                cdResId = state.model.cdResId,
                errorText = state.model.text.asString(context),
                onRefreshClick = {
                    viewModel.processCommand(CreateQuestionCommand.Refresh)
                }
            )
        }
    }
}

@Composable
private fun CreateQuestionCard(
    modifier: Modifier = Modifier,
    selectedType: QuestionType,
    queryText: String,
    explanation: String,
    options: List<QuestionOption>,
    textAnswers: List<TextAnswer>,
    canCreate: Boolean,
    isCreating: Boolean,
    onTypeChanged: (QuestionType) -> Unit,
    onTextQueryChanged: (String) -> Unit,
    onExplanationQueryChanged: (String) -> Unit,
    onAddAnswerClick: () -> Unit,
    onTextAnswerChanged: (Int, String) -> Unit,
    onTextAnswerMatcherSelected: (Int, MatcherType) -> Unit,
    onRemoveTextAnswer: (Int) -> Unit,
    onOptionTextChanged: (Int, String) -> Unit,
    onToggleOptionCorrect: (Int) -> Unit,
    onRemoveOption: (Int) -> Unit,
    onCancelClick: () -> Unit,
    onCreateTaskClick: () -> Unit
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
        LazyColumn(modifier = Modifier.padding(vertical = 24.dp, horizontal = 16.dp)) {

            val typeOptions = typeOptions()

            val selectedTypeTitle =
                typeOptions.first { it.value == selectedType }.title

            item {
                Text(
                    text = stringResource(R.string.question_type),
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 16.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                DropdownField(
                    options = typeOptions.map { it.title },
                    selected = selectedTypeTitle,
                    placeholder = "",
                    onSelectedChange = { title ->
                        val type = typeOptions.first { it.title == title }.value
                        onTypeChanged(type)
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))
            }

            item {
                Text(
                    text = stringResource(R.string.question_text),
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 16.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                InputField(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 120.dp, max = 800.dp),
                    query = queryText,
                    placeHolderText = stringResource(R.string.input_text_question),
                    onQueryChanged = onTextQueryChanged
                )

                Spacer(modifier = Modifier.height(16.dp))
            }

            item {
                Text(
                    text = stringResource(R.string.explanation_not_required),
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 16.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                InputField(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 56.dp, max = 120.dp),
                    query = explanation,
                    placeHolderText = stringResource(R.string.explain_right_answer),
                    onQueryChanged = onExplanationQueryChanged
                )

                Spacer(modifier = Modifier.height(16.dp))
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = when (selectedType) {
                            QuestionType.TEXT -> stringResource(R.string.right_answers)
                            else -> stringResource(R.string.answer_options)
                        },
                        fontSize = 16.sp,
                        modifier = Modifier.widthIn(max = 116.dp),
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.weight(1f))

                    Button(
                        modifier = Modifier,
                        onClick = onAddAnswerClick,
                        colors = ButtonDefaults.buttonColors()
                            .copy(containerColor = MaterialTheme.colorScheme.surfaceBright),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text(
                            text = when (selectedType) {
                                QuestionType.TEXT -> stringResource(R.string.add_answer)
                                else -> stringResource(R.string.add_option)
                            },
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 16.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }

            when (selectedType) {
                QuestionType.TEXT -> {
                    items(
                        items = textAnswers,
                        key = { answer -> answer.id }
                    ) { answer ->
                        TextAnswerItem(
                            answer = answer,
                            onValueChange = { newValue ->
                                onTextAnswerChanged(
                                    answer.id,
                                    newValue
                                )
                            },
                            onMatcherSelected = { newType ->
                                onTextAnswerMatcherSelected(
                                    answer.id,
                                    newType
                                )
                            },
                            onRemove = { onRemoveTextAnswer(answer.id) }
                        )


                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }

                else -> {
                    items(
                        items = options,
                        key = { option -> option.id }
                    ) { option ->
                        OptionItem(
                            option = option,
                            type = selectedType,
                            onTextChange = { newText ->
                                onOptionTextChanged(option.id, newText)
                            },
                            onToggleCorrect = {
                                onToggleOptionCorrect(option.id)
                            },
                            onRemove = {
                                onRemoveOption(option.id)
                            }
                        )

                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Button(
                        modifier = Modifier
                            .weight(1f)
                            .height(64.dp),
                        onClick = onCancelClick,
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
                        onClick = onCreateTaskClick,
                        enabled = canCreate && !isCreating,
                        colors = ButtonDefaults.buttonColors()
                            .copy(
                                containerColor = MaterialTheme.colorScheme.primary,
                                disabledContainerColor = MaterialTheme.colorScheme.primary.copy(
                                    alpha = 0.3f
                                ),
                                disabledContentColor = Color.White.copy(
                                    alpha = 0.3f
                                )
                            ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        if (!isCreating) {
                            Text(
                                text = stringResource(R.string.create),
                                fontSize = 18.sp,
                                color = Color.White
                            )
                        }

                        if (isCreating) {
                            CircularProgressIndicator(
                                strokeWidth = 2.dp,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TextAnswerItem(
    modifier: Modifier = Modifier,
    answer: TextAnswer,
    onValueChange: (String) -> Unit,
    onMatcherSelected: (MatcherType) -> Unit,
    onRemove: () -> Unit
) {
    val options = matcherOptions()
    val selectedTitle = options.first { it.value == answer.matcherType }.title

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        MatcherPicker(
            modifier = Modifier.weight(1f),
            selectedTitle = selectedTitle,
            options = options.map { it.title },
            onSelectedChange = { title ->
                onMatcherSelected(options.first { it.title == title }.value)
            }
        )

        Spacer(Modifier.width(12.dp))

        InputField(
            modifier = Modifier
                .weight(2f)
                .heightIn(min = 56.dp),
            query = answer.value,
            placeHolderText = stringResource(R.string.right_answer),
            onQueryChanged = onValueChange
        )

        Spacer(Modifier.width(8.dp))

        Icon(
            painter = painterResource(R.drawable.ic_trash),
            contentDescription = stringResource(R.string.delete),
            modifier = Modifier
                .size(20.dp)
                .clickable(onClick = onRemove),
            tint = MaterialTheme.colorScheme.onError
        )
    }
}

@Composable
private fun OptionItem(
    modifier: Modifier = Modifier,
    option: QuestionOption,
    type: QuestionType,
    onTextChange: (String) -> Unit,
    onToggleCorrect: () -> Unit,
    onRemove: () -> Unit
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        when (type) {
            QuestionType.MULTIPLE -> {
                Checkbox(
                    checked = option.isCorrect,
                    onCheckedChange = { onToggleCorrect() }
                )
            }

            else -> {
                RadioButton(
                    selected = option.isCorrect,
                    onClick = onToggleCorrect
                )
            }
        }

        Spacer(Modifier.width(8.dp))

        InputField(
            modifier = Modifier
                .weight(1f)
                .heightIn(min = 56.dp),
            query = option.text,
            placeHolderText = stringResource(R.string.option) + " ${option.id}",
            onQueryChanged = onTextChange
        )

        Spacer(Modifier.width(8.dp))

        Icon(
            painter = painterResource(R.drawable.ic_trash),
            contentDescription = stringResource(R.string.delete),
            modifier = Modifier
                .size(20.dp)
                .clickable(onClick = onRemove),
            tint = MaterialTheme.colorScheme.onError
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MatcherPicker(
    modifier: Modifier = Modifier,
    selectedTitle: String,
    options: List<String>,
    onSelectedChange: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .menuAnchor()
                .width(120.dp)
                .height(56.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(MaterialTheme.colorScheme.onPrimary)
                .clickable { expanded = true }
                .padding(horizontal = 12.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = selectedTitle,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )

                Spacer(Modifier.width(6.dp))

                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            }
        }

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            containerColor = MaterialTheme.colorScheme.onPrimary
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = option,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    onClick = {
                        onSelectedChange(option)
                        expanded = false
                    }
                )
            }
        }
    }
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

@Composable
private fun InputField(
    modifier: Modifier = Modifier,
    query: String,
    placeHolderText: String,
    onQueryChanged: (String) -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

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
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        interactionSource = interactionSource,
        colors = OutlinedTextFieldDefaults.colors(
            unfocusedBorderColor = MaterialTheme.colorScheme.surfaceBright,
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedPlaceholderColor = MaterialTheme.colorScheme.surfaceBright,
        )
    )
}

private data class TypeOption(val title: String, val value: QuestionType)

private fun typeOptions(): List<TypeOption> = listOf(
    TypeOption("Одиночный выбор", QuestionType.SINGLE),
    TypeOption("Текстовый ответ", QuestionType.TEXT),
    TypeOption("Множественный выбор", QuestionType.MULTIPLE),
)

private data class MatcherOption(val title: String, val value: MatcherType)

private fun matcherOptions(): List<MatcherOption> = listOf(
    MatcherOption("Точно", MatcherType.EQUALS),
    MatcherOption("Содержит", MatcherType.CONTAINS),
    MatcherOption("Содержит (без регистра)", MatcherType.ICONTAINS),
)
