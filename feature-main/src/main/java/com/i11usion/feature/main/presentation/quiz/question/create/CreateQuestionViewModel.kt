package com.i11usion.feature.main.presentation.quiz.question.create

import AppError
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.i11usion.core.domain.model.network.AppResult
import com.i11usion.core.domain.model.quiz.MatcherType
import com.i11usion.core.domain.model.quiz.Question
import com.i11usion.core.domain.model.quiz.QuestionOption
import com.i11usion.core.domain.model.quiz.QuestionType
import com.i11usion.core.domain.model.quiz.TextAnswer
import com.i11usion.core.domain.usecase.quiz.CreateQuestionUseCase
import com.i11usion.core.ui.model.ErrorScreenModel
import com.i11usion.core.ui.model.UiError
import com.i11usion.feature.main.manager.SnackbarManager
import com.i11usion.feature.main.mapper.toScreenUiError
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

class CreateQuestionViewModel @Inject constructor(
    private val createQuestionUseCase: CreateQuestionUseCase,
    private val snackbarManager: SnackbarManager
) : ViewModel() {

    private val _state = MutableStateFlow<CreateQuestionUiState>(
        CreateQuestionUiState.Content(canCreate = false)
    )
    val state = _state.asStateFlow()

    private val _events = MutableSharedFlow<CreateQuestionEvent>()
    val events = _events.asSharedFlow()

    fun processCommand(command: CreateQuestionCommand) {
        when (command) {
            is CreateQuestionCommand.ChangeType -> changeType(command.type)
            is CreateQuestionCommand.ChangeText -> updateContent { it.copy(text = command.value) }
            is CreateQuestionCommand.ChangeExplanation -> updateContent { it.copy(explanation = command.value) }

            CreateQuestionCommand.AddAnswerOrOption -> addAnswerOrOption()

            is CreateQuestionCommand.ChangeOptionText ->
                updateContent { c ->
                    c.copy(options = c.options.map { opt ->
                        if (opt.id == command.id) opt.copy(text = command.value) else opt
                    })
                }

            is CreateQuestionCommand.ToggleOptionCorrect -> toggleOptionCorrect(command.id)

            is CreateQuestionCommand.RemoveOption ->
                updateContent { c ->
                    val filtered = c.options.filterNot { it.id == command.id }

                    val reIndexed = filtered.mapIndexed { index, option ->
                        option.copy(id = index + 1)
                    }

                    c.copy(options = reIndexed)
                        .normalizeSingleCorrectIfNeeded()
                }

            is CreateQuestionCommand.ChangeTextAnswerValue ->
                updateContent { c ->
                    c.copy(textAnswers = c.textAnswers.map { ans ->
                        if (ans.id == command.id) ans.copy(value = command.value) else ans
                    })
                }

            is CreateQuestionCommand.SetTextAnswerMatcher ->
                updateContent { c ->
                    c.copy(textAnswers = c.textAnswers.map { ans ->
                        if (ans.id == command.id) ans.copy(matcherType = command.type) else ans
                    })
                }

            is CreateQuestionCommand.RemoveTextAnswer ->
                updateContent { c ->
                    val newList = c.textAnswers
                        .filterNot { it.id == command.id }
                        .ensureMinTextAnswersForType(c.questionType)

                    c.copy(textAnswers = newList)
                }

            CreateQuestionCommand.Create -> create()

            CreateQuestionCommand.Cancel -> {
                viewModelScope.launch { _events.emit(CreateQuestionEvent.Close) }
            }

            CreateQuestionCommand.Refresh -> onErrorRefreshClick()
        }
    }

    private fun onErrorRefreshClick() {
        when (_state.value) {
            is CreateQuestionUiState.Content -> _state.value =
                (_state.value as CreateQuestionUiState.Content).copy(fullScreenError = null)

            else -> _state.value = CreateQuestionUiState.Content()
        }
    }

    private fun updateContent(
        reducer: (CreateQuestionUiState.Content) -> CreateQuestionUiState.Content
    ) {
        _state.update { current ->
            val content =
                current as? CreateQuestionUiState.Content ?: CreateQuestionUiState.Content()
            val updated = reducer(content)
            updated.copy(canCreate = computeCanCreate(updated))
        }
    }

    private fun changeType(type: QuestionType) {
        updateContent { c ->
            val base = c.copy(questionType = type)

            when (type) {
                QuestionType.TEXT -> {
                    base.copy(
                        options = emptyList(),
                        textAnswers = base.textAnswers.ensureMinTextAnswersForType(type)
                    )
                }

                QuestionType.SINGLE, QuestionType.MULTIPLE -> {
                    val withOptions = base.copy(
                        options = base.options.ensureMinOptionsForType(type),
                        textAnswers = emptyList()
                    )
                    if (type == QuestionType.SINGLE) withOptions.normalizeSingleCorrectIfNeeded() else withOptions
                }
            }
        }
    }

    private fun addAnswerOrOption() {
        updateContent { c ->
            when (c.questionType) {
                QuestionType.TEXT -> {
                    val nextId = (c.textAnswers.maxOfOrNull { it.id } ?: 0) + 1
                    c.copy(
                        textAnswers = c.textAnswers + TextAnswer(
                            id = nextId,
                            matcherType = MatcherType.EQUALS,
                            value = ""
                        )
                    )
                }

                QuestionType.SINGLE, QuestionType.MULTIPLE -> {
                    val nextId = c.options.size + 1
                    c.copy(
                        options = c.options + QuestionOption(
                            id = nextId,
                            text = "",
                            isCorrect = false
                        )
                    )
                }
            }
        }
    }

    private fun toggleOptionCorrect(optionId: Int) {
        updateContent { c ->
            when (c.questionType) {
                QuestionType.MULTIPLE -> {
                    c.copy(
                        options = c.options.map { opt ->
                            if (opt.id == optionId) opt.copy(isCorrect = !opt.isCorrect) else opt
                        }
                    )
                }

                QuestionType.SINGLE -> {
                    c.copy(
                        options = c.options.map { opt ->
                            opt.copy(isCorrect = opt.id == optionId)
                        }
                    )
                }

                QuestionType.TEXT -> c
            }
        }
    }

    private fun create() {
        val content = _state.value as? CreateQuestionUiState.Content ?: return
        if (!content.canCreate || content.isCreating) return

        viewModelScope.launch {
            _state.update {
                (it as CreateQuestionUiState.Content).copy(isCreating = true, canCreate = false)
            }

            val question = content.toDomainForCreate()
            when (val result = createQuestionUseCase(question)) {

                is AppResult.Success -> {
                    _events.emit(CreateQuestionEvent.Created)
                }

                is AppResult.Failure -> {
                    _state.update { cur ->
                        val c = cur as? CreateQuestionUiState.Content ?: return@update cur
                        c.copy(isCreating = false, canCreate = true)
                    }
                    showUiError(result.error)
                }
            }
        }
    }

    private fun computeCanCreate(c: CreateQuestionUiState.Content): Boolean {
        val questionTextOk = c.text.trim().isNotEmpty()

        val answersOk = when (c.questionType) {
            QuestionType.TEXT ->
                c.textAnswers.any { it.value.trim().isNotEmpty() }

            QuestionType.SINGLE, QuestionType.MULTIPLE -> {
                val nonEmptyOptions = c.options.count { it.text.trim().isNotEmpty() }
                nonEmptyOptions >= 2 && c.options.any { it.isCorrect }
            }
        }

        return questionTextOk && answersOk && !c.isCreating
    }

    private fun List<QuestionOption>.ensureMinOptionsForType(type: QuestionType): List<QuestionOption> {
        if (type == QuestionType.TEXT) return this
        return ifEmpty { listOf(QuestionOption(id = 1, text = "", isCorrect = false)) }
    }

    private fun List<TextAnswer>.ensureMinTextAnswersForType(type: QuestionType): List<TextAnswer> {
        if (type != QuestionType.TEXT) return this
        return ifEmpty { listOf(TextAnswer(id = 1, matcherType = MatcherType.EQUALS, value = "")) }
    }

    private fun CreateQuestionUiState.Content.normalizeSingleCorrectIfNeeded(): CreateQuestionUiState.Content {
        if (questionType != QuestionType.SINGLE) return this
        val firstCorrectId = options.firstOrNull { it.isCorrect }?.id
        return if (firstCorrectId == null) {
            copy(options = options.map { it.copy(isCorrect = false) })
        } else {
            copy(options = options.map { it.copy(isCorrect = it.id == firstCorrectId) })
        }
    }

    private fun CreateQuestionUiState.Content.toDomainForCreate(): Question {
        val exp = explanation.trim().takeIf { it.isNotEmpty() }

        return when (questionType) {
            QuestionType.TEXT -> {
                Question(
                    id = 0,
                    text = text.trim(),
                    type = questionType,
                    explanation = exp,
                    options = emptyList(),
                    textAnswers = textAnswers
                        .map { it.copy(value = it.value.trim()) }
                        .filter { it.value.isNotEmpty() }
                )
            }

            QuestionType.SINGLE, QuestionType.MULTIPLE -> {
                Question(
                    id = 0,
                    text = text.trim(),
                    type = questionType,
                    explanation = exp,
                    options = options
                        .map { it.copy(text = it.text.trim()) }
                        .filter { it.text.isNotEmpty() },
                    textAnswers = emptyList()
                )
            }
        }
    }

    private fun showUiError(error: AppError) {
        when (val uiError = error.toScreenUiError()) {
            is UiError.SnackBar -> snackbarManager.show(uiError.message)

            is UiError.FullScreen -> {
                val current = _state.value
                if (current is CreateQuestionUiState.Content) {
                    _state.value = current.copy(fullScreenError = uiError.model)
                } else {
                    _state.value = CreateQuestionUiState.Error(uiError.model)
                }
            }
        }
    }
}

sealed interface CreateQuestionEvent {
    data object Created : CreateQuestionEvent
    data object Close : CreateQuestionEvent
}

sealed interface CreateQuestionCommand {
    data class ChangeType(val type: QuestionType) : CreateQuestionCommand
    data class ChangeText(val value: String) : CreateQuestionCommand
    data class ChangeExplanation(val value: String) : CreateQuestionCommand

    data object AddAnswerOrOption : CreateQuestionCommand

    data class ChangeOptionText(val id: Int, val value: String) : CreateQuestionCommand
    data class ToggleOptionCorrect(val id: Int) : CreateQuestionCommand
    data class RemoveOption(val id: Int) : CreateQuestionCommand

    data class ChangeTextAnswerValue(val id: Int, val value: String) : CreateQuestionCommand
    data class SetTextAnswerMatcher(val id: Int, val type: MatcherType) : CreateQuestionCommand
    data class RemoveTextAnswer(val id: Int) : CreateQuestionCommand

    data object Create : CreateQuestionCommand
    data object Cancel : CreateQuestionCommand
    data object Refresh : CreateQuestionCommand
}

sealed interface CreateQuestionUiState {
    data object Initial : CreateQuestionUiState

    data class Content(
        val questionType: QuestionType = QuestionType.SINGLE,
        val text: String = "",
        val explanation: String = "",

        val options: List<QuestionOption> = listOf(
            QuestionOption(id = 1, text = "", isCorrect = false)
        ),
        val textAnswers: List<TextAnswer> = listOf(
            TextAnswer(id = 1, matcherType = MatcherType.EQUALS, value = "")
        ),

        val canCreate: Boolean = false,
        val isCreating: Boolean = false,
        val fullScreenError: ErrorScreenModel? = null
    ) : CreateQuestionUiState

    data class Error(val model: ErrorScreenModel) : CreateQuestionUiState
}
