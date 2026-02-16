package com.i11usion.feature.main.presentation.quiz.question.update

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.i11usion.core.domain.model.quiz.MatcherType
import com.i11usion.core.domain.model.quiz.Question
import com.i11usion.core.domain.model.quiz.QuestionOption
import com.i11usion.core.domain.model.quiz.QuestionType
import com.i11usion.core.domain.model.quiz.TextAnswer
import com.i11usion.core.domain.usecase.quiz.GetQuestionByIdUseCase
import com.i11usion.core.domain.usecase.quiz.UpdateQuestionUseCase
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

class UpdateQuestionViewModel @Inject constructor(
    private val getQuestionByIdUseCase: GetQuestionByIdUseCase,
    private val updateQuestionUseCase: UpdateQuestionUseCase
) : ViewModel() {

    private val _state = MutableStateFlow<UpdateQuestionUiState>(UpdateQuestionUiState.Initial)
    val state = _state.asStateFlow()

    private val _events = MutableSharedFlow<UpdateQuestionEvent>()
    val events = _events.asSharedFlow()

    private var initializedId: Int? = null

    init {
        _state.value = UpdateQuestionUiState.Loading
    }

    fun process(command: UpdateQuestionCommand) {
        when (command) {
            is UpdateQuestionCommand.Init -> init(command.questionId)

            is UpdateQuestionCommand.ChangeType -> changeType(command.type)
            is UpdateQuestionCommand.ChangeText -> updateContent { it.copy(text = command.value) }
            is UpdateQuestionCommand.ChangeExplanation -> updateContent { it.copy(explanation = command.value) }

            UpdateQuestionCommand.AddAnswerOrOption -> addAnswerOrOption()

            is UpdateQuestionCommand.ChangeOptionText ->
                updateContent { c ->
                    c.copy(options = c.options.map { opt ->
                        if (opt.id == command.id) opt.copy(text = command.value) else opt
                    })
                }

            is UpdateQuestionCommand.ToggleOptionCorrect -> toggleOptionCorrect(command.id)

            is UpdateQuestionCommand.RemoveOption ->
                updateContent { c ->
                    // удаляем всегда последний (как вы обсуждали ранее)
                    val newList = c.options.dropLast(1).ensureMinOptionsForType(c.questionType)
                    c.copy(options = newList).normalizeSingleCorrectIfNeeded()
                }

            is UpdateQuestionCommand.ChangeTextAnswerValue ->
                updateContent { c ->
                    c.copy(textAnswers = c.textAnswers.map { ans ->
                        if (ans.id == command.id) ans.copy(value = command.value) else ans
                    })
                }

            is UpdateQuestionCommand.SetTextAnswerMatcher ->
                updateContent { c ->
                    c.copy(textAnswers = c.textAnswers.map { ans ->
                        if (ans.id == command.id) ans.copy(matcherType = command.type) else ans
                    })
                }

            is UpdateQuestionCommand.RemoveTextAnswer ->
                updateContent { c ->
                    val newList = c.textAnswers.dropLast(1).ensureMinTextAnswersForType(c.questionType)
                    c.copy(textAnswers = newList)
                }

            UpdateQuestionCommand.Save -> save()

            UpdateQuestionCommand.Cancel -> {
                viewModelScope.launch { _events.emit(UpdateQuestionEvent.Close) }
            }
        }
    }

    private fun init(questionId: Int) {
        if (initializedId == questionId) return
        initializedId = questionId

        _state.value = UpdateQuestionUiState.Loading

        viewModelScope.launch {
            try {
                val question = getQuestionByIdUseCase(questionId)
                _state.value = question.toUiContent()
            } catch (t: Throwable) {
                _state.value = UpdateQuestionUiState.Error(t.message ?: "Failed to load question")
                _events.emit(UpdateQuestionEvent.ShowError(t.message ?: "Failed to load question"))
            }
        }
    }

    private fun updateContent(
        reducer: (UpdateQuestionUiState.Content) -> UpdateQuestionUiState.Content
    ) {
        _state.update { current ->
            val content = current as? UpdateQuestionUiState.Content ?: return@update current
            val updated = reducer(content)
            updated.copy(canSave = computeCanSave(updated))
        }
    }

    private fun changeType(type: QuestionType) {
        updateContent { c ->
            val base = c.copy(questionType = type)

            when (type) {
                QuestionType.TEXT -> {
                    base.copy(
                        options = emptyList(),
                        textAnswers = base.textAnswers.ensureMinTextAnswersForType(type).reindexTextAnswers()
                    )
                }

                QuestionType.SINGLE, QuestionType.MULTIPLE -> {
                    val withOptions = base.copy(
                        options = base.options.ensureMinOptionsForType(type).reindexOptions(),
                        textAnswers = emptyList()
                    )
                    if (type == QuestionType.SINGLE) withOptions.normalizeSingleCorrectIfNeeded()
                    else withOptions
                }
            }
        }
    }

    private fun addAnswerOrOption() {
        updateContent { c ->
            when (c.questionType) {
                QuestionType.TEXT -> {
                    val nextId = c.textAnswers.size + 1
                    c.copy(
                        textAnswers = (c.textAnswers + TextAnswer(
                            id = nextId,
                            matcherType = MatcherType.EQUALS,
                            value = ""
                        )).reindexTextAnswers()
                    )
                }

                QuestionType.SINGLE, QuestionType.MULTIPLE -> {
                    val nextId = c.options.size + 1
                    c.copy(
                        options = (c.options + QuestionOption(
                            id = nextId,
                            text = "",
                            isCorrect = false
                        )).reindexOptions()
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

    private fun save() {
        val content = _state.value as? UpdateQuestionUiState.Content ?: return
        if (!content.canSave || content.isSaving) return

        viewModelScope.launch {
            _state.update { cur ->
                val c = cur as? UpdateQuestionUiState.Content ?: return@update cur
                c.copy(isSaving = true, canSave = false)
            }

            try {
                val domain = content.toDomainForUpdate()
                updateQuestionUseCase(content.questionId, domain)
                _events.emit(UpdateQuestionEvent.Saved)
            } catch (t: Throwable) {
                _state.update { cur ->
                    val c = cur as? UpdateQuestionUiState.Content ?: return@update cur
                    c.copy(isSaving = false, canSave = computeCanSave(c))
                }
                _events.emit(UpdateQuestionEvent.ShowError(t.message ?: "Failed to update question"))
            }
        }
    }

    /** ===== Validation / mapping ===== */

    private fun computeCanSave(c: UpdateQuestionUiState.Content): Boolean {
        val questionTextOk = c.text.trim().isNotEmpty()

        val answersOk = when (c.questionType) {
            QuestionType.TEXT ->
                c.textAnswers.any { it.value.trim().isNotEmpty() }

            QuestionType.SINGLE, QuestionType.MULTIPLE -> {
                val nonEmptyOptions = c.options.count { it.text.trim().isNotEmpty() }
                nonEmptyOptions >= 2 && c.options.any { it.isCorrect }
            }
        }

        return questionTextOk && answersOk && !c.isSaving
    }

    private fun List<QuestionOption>.ensureMinOptionsForType(type: QuestionType): List<QuestionOption> {
        if (type == QuestionType.TEXT) return this
        return if (this.isEmpty()) listOf(QuestionOption(id = 1, text = "", isCorrect = false)) else this
    }

    private fun List<TextAnswer>.ensureMinTextAnswersForType(type: QuestionType): List<TextAnswer> {
        if (type != QuestionType.TEXT) return this
        return if (this.isEmpty()) listOf(TextAnswer(id = 1, matcherType = MatcherType.EQUALS, value = "")) else this
    }

    private fun UpdateQuestionUiState.Content.normalizeSingleCorrectIfNeeded(): UpdateQuestionUiState.Content {
        if (questionType != QuestionType.SINGLE) return this
        val firstCorrectId = options.firstOrNull { it.isCorrect }?.id
        return if (firstCorrectId == null) {
            copy(options = options.map { it.copy(isCorrect = false) })
        } else {
            copy(options = options.map { it.copy(isCorrect = it.id == firstCorrectId) })
        }
    }

    private fun List<QuestionOption>.reindexOptions(): List<QuestionOption> =
        mapIndexed { idx, opt -> opt.copy(id = idx + 1) }

    private fun List<TextAnswer>.reindexTextAnswers(): List<TextAnswer> =
        mapIndexed { idx, ans -> ans.copy(id = idx + 1) }

    private fun Question.toUiContent(): UpdateQuestionUiState.Content {
        val base = UpdateQuestionUiState.Content(
            questionId = id,
            questionType = type,
            text = text,
            explanation = explanation.orEmpty(),
            options = options.reindexOptions(),
            textAnswers = textAnswers.reindexTextAnswers(),
            canSave = false,
            isSaving = false
        )

        // ensure минимум для текущего типа (и single нормализация)
        val ensured = when (type) {
            QuestionType.TEXT -> base.copy(
                options = emptyList(),
                textAnswers = base.textAnswers.ensureMinTextAnswersForType(type).reindexTextAnswers()
            )

            QuestionType.SINGLE, QuestionType.MULTIPLE -> {
                val withOpt = base.copy(
                    options = base.options.ensureMinOptionsForType(type).reindexOptions(),
                    textAnswers = emptyList()
                )
                if (type == QuestionType.SINGLE) withOpt.normalizeSingleCorrectIfNeeded() else withOpt
            }
        }

        return ensured.copy(canSave = computeCanSave(ensured))
    }

    private fun UpdateQuestionUiState.Content.toDomainForUpdate(): Question {
        val exp = explanation.trim().takeIf { it.isNotEmpty() }

        return when (questionType) {
            QuestionType.TEXT -> {
                Question(
                    id = questionId,
                    text = text.trim(),
                    type = questionType,
                    explanation = exp,
                    options = emptyList(),
                    textAnswers = textAnswers
                        .map { it.copy(value = it.value.trim()) }
                        .filter { it.value.isNotEmpty() }
                        .reindexTextAnswers()
                )
            }

            QuestionType.SINGLE, QuestionType.MULTIPLE -> {
                Question(
                    id = questionId,
                    text = text.trim(),
                    type = questionType,
                    explanation = exp,
                    options = options
                        .map { it.copy(text = it.text.trim()) }
                        .filter { it.text.isNotEmpty() }
                        .reindexOptions(),
                    textAnswers = emptyList()
                )
            }
        }
    }
}

sealed interface UpdateQuestionUiState {
    data object Initial: UpdateQuestionUiState
    data object Loading : UpdateQuestionUiState

    data class Content(
        val questionId: Int,
        val questionType: QuestionType,
        val text: String,
        val explanation: String,

        val options: List<QuestionOption>,
        val textAnswers: List<TextAnswer>,

        val canSave: Boolean,
        val isSaving: Boolean
    ) : UpdateQuestionUiState

    data class Error(val message: String) : UpdateQuestionUiState
}

sealed interface UpdateQuestionEvent {
    data object Saved : UpdateQuestionEvent
    data object Close : UpdateQuestionEvent
    data class ShowError(val message: String) : UpdateQuestionEvent
}

sealed interface UpdateQuestionCommand {
    data class Init(val questionId: Int) : UpdateQuestionCommand

    data class ChangeType(val type: QuestionType) : UpdateQuestionCommand
    data class ChangeText(val value: String) : UpdateQuestionCommand
    data class ChangeExplanation(val value: String) : UpdateQuestionCommand

    data object AddAnswerOrOption : UpdateQuestionCommand

    data class ChangeOptionText(val id: Int, val value: String) : UpdateQuestionCommand
    data class ToggleOptionCorrect(val id: Int) : UpdateQuestionCommand
    data class RemoveOption(val id: Int) : UpdateQuestionCommand

    data class ChangeTextAnswerValue(val id: Int, val value: String) : UpdateQuestionCommand
    data class SetTextAnswerMatcher(val id: Int, val type: MatcherType) : UpdateQuestionCommand
    data class RemoveTextAnswer(val id: Int) : UpdateQuestionCommand

    data object Save : UpdateQuestionCommand
    data object Cancel : UpdateQuestionCommand
}
