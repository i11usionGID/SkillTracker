package com.i11usion.feature.main.presentation.quiz.list

import AppError
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.i11usion.core.domain.model.network.AppResult
import com.i11usion.core.domain.model.quiz.QuizStatus
import com.i11usion.core.domain.model.user.Role
import com.i11usion.core.domain.model.user.User
import com.i11usion.core.domain.usecase.quiz.DeleteQuizUseCase
import com.i11usion.core.domain.usecase.quiz.GetMyQuizAssignmentsUseCase
import com.i11usion.core.domain.usecase.quiz.GetQuizListUseCase
import com.i11usion.core.domain.usecase.session.GetUserInfoUseCase
import com.i11usion.core.ui.formatter.formatInstantToStringData
import com.i11usion.core.ui.formatter.formatInstantToStringDataNullable
import com.i11usion.core.ui.model.ErrorScreenModel
import com.i11usion.core.ui.model.UiError
import com.i11usion.core.ui.model.UiText
import com.i11usion.feature.main.manager.SnackbarManager
import com.i11usion.feature.main.mapper.toScreenUiError
import com.i11usion.feature.main.presentation.quiz.list.mapper.quizToQuizEmployeeUi
import com.i11usion.feature.main.presentation.quiz.list.mapper.quizToQuizManagerUi
import com.i11usion.feature.main.presentation.quiz.list.model.QuizEmployeeUiModel
import com.i11usion.feature.main.presentation.quiz.list.model.QuizFilters
import com.i11usion.feature.main.presentation.quiz.list.model.QuizManagerUiModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

class QuizViewModel @Inject constructor(
    private val getUserInfoUseCase: GetUserInfoUseCase,
    private val getQuizListUseCase: GetQuizListUseCase,
    private val getMyQuizAssignmentsUseCase: GetMyQuizAssignmentsUseCase,
    private val deleteQuizUseCase: DeleteQuizUseCase,
    private val snackbarManager: SnackbarManager
) : ViewModel() {

    private companion object {
        const val RETRY_DELAY_MS = 10_000L
    }

    private val filters = MutableStateFlow(QuizFilters())
    private val currentUser = MutableStateFlow<User?>(null)
    private val reloadQuizzes = MutableSharedFlow<Unit>(
        extraBufferCapacity = 1
    )
    private val _state = MutableStateFlow<QuizUiState>(QuizUiState.Initial)
    val state = _state.asStateFlow()

    init {
        observeFiltersAndLoadQuizzes()
        loadInitial()
    }

    private fun loadInitial() {
        viewModelScope.launch {
            _state.value = QuizUiState.Loading

            runCatching {
                val user = getUserInfoUseCase()
                requireNotNull(user) { "Не удалось получить пользователя" }
                user
            }.fold(
                onSuccess = { user ->
                    currentUser.value = user
                    _state.value = QuizUiState.Content(
                        user = user,
                        quizFilters = QuizFilters(),
                        quizList = emptyList(),
                        assignmentsList = emptyList(),
                        isListLoading = true
                    )
                    reloadQuizzes.tryEmit(Unit)
                },
                onFailure = { t ->
                    showUiError(AppError.Validation(message = "Не удалось загрузить данные"))
                }
            )
        }
    }

    fun processCommand(command: QuizCommand) {
        when (command) {
            is QuizCommand.InputSearchQuery -> {
                val raw = command.query

                _state.update { st ->
                    when (st) {
                        is QuizUiState.Content -> st.copy(
                            quizFilters = st.quizFilters.copy(query = raw)
                        )

                        else -> st
                    }
                }

                filters.update { it.copy(query = raw.trim()) }
            }

            is QuizCommand.SelectStatus -> {
                _state.update { st ->
                    when (st) {
                        is QuizUiState.Content -> st.copy(
                            quizFilters = st.quizFilters.copy(status = command.status)
                        )

                        else -> st
                    }
                }
                filters.update { it.copy(status = command.status) }
            }

            is QuizCommand.DeleteQuiz -> {
                viewModelScope.launch {
                    viewModelScope.launch {
                        when (val result = deleteQuizUseCase(command.quizId)) {
                            is AppResult.Success -> reloadQuizzes.tryEmit(Unit)
                            is AppResult.Failure -> showUiError(result.error)
                        }
                    }
                }
            }

            QuizCommand.Refresh -> {
                reloadQuizzes.tryEmit(Unit)
            }
        }
    }

    @OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
    private fun observeFiltersAndLoadQuizzes() {
        val queryFlow = filters
            .map { it.query }
            .distinctUntilChanged()
            .debounce(300)

        val statusFlow = filters
            .map { it.status }
            .distinctUntilChanged()

        val filtersFlow = combine(queryFlow, statusFlow) { q, s ->
            QuizFilters(query = q, status = s)
        }.distinctUntilChanged()

        val triggerFlow = merge(
            filtersFlow,
            reloadQuizzes.map { filters.value }
        )

        combine(
            triggerFlow,
            currentUser.filterNotNull()
        ) { f, user ->
            user to f
        }
            .onEach { (user, currentFilters) ->
                _state.update { st ->
                    when (st) {
                        is QuizUiState.Content -> st.copy(
                            user = user,
                            quizFilters = currentFilters,
                            isListLoading = true,
                            fullScreenError = null
                        )

                        else -> QuizUiState.Content(
                            user = user,
                            quizFilters = currentFilters,
                            quizList = emptyList(),
                            assignmentsList = emptyList(),
                            isListLoading = true,
                            fullScreenError = null
                        )
                    }
                }
            }
            .mapLatest { (user, currentFilters) ->
                retryRequest {
                    val status = when (currentFilters.status) {
                        QuizStatus.PUBLISHED -> "published"
                        QuizStatus.DRAFT -> "draft"
                        null -> null
                    }

                    if (user.role == Role.MANAGER) {
                        when (val res = getQuizListUseCase(
                            page = null,
                            perPage = null,
                            search = currentFilters.query,
                            status = status
                        )) {
                            is AppResult.Success -> {
                                val ui = res.data.map {
                                    it.quizToQuizManagerUi(::formatInstantToStringData)
                                }
                                AppResult.Success(
                                    Loaded(
                                        user = user,
                                        manager = ui,
                                        employee = emptyList()
                                    )
                                )
                            }

                            is AppResult.Failure -> {
                                AppResult.Failure(res.error)
                            }
                        }
                    } else {
                        when (val res = getMyQuizAssignmentsUseCase()) {
                            is AppResult.Success -> {
                                val ui = res.data.map {
                                    it.quizToQuizEmployeeUi(::formatInstantToStringDataNullable)
                                }
                                AppResult.Success(
                                    Loaded(
                                        user = user,
                                        manager = emptyList(),
                                        employee = ui
                                    )
                                )
                            }

                            is AppResult.Failure -> {
                                AppResult.Failure(res.error)
                            }
                        }
                    }
                }
            }
            .onEach { result ->
                when (result) {
                    is AppResult.Success -> {
                        val uiFilters =
                            (_state.value as? QuizUiState.Content)?.quizFilters
                                ?: QuizFilters(
                                    query = filters.value.query,
                                    status = filters.value.status
                                )

                        _state.value = QuizUiState.Content(
                            user = result.data.user,
                            quizFilters = uiFilters,
                            quizList = result.data.manager,
                            assignmentsList = result.data.employee,
                            isListLoading = false,
                            fullScreenError = null
                        )
                    }

                    is AppResult.Failure -> {
                        val cur = _state.value
                        if (cur is QuizUiState.Content) {
                            _state.value = cur.copy(isListLoading = false)
                        }
                        showUiError(result.error)
                    }
                }
            }
            .launchIn(viewModelScope)
    }

    private suspend fun <T> retryRequest(
        block: suspend () -> AppResult<T>
    ): AppResult<T> {
        while (true) {
            when (val result = block()) {
                is AppResult.Success -> return result
                is AppResult.Failure -> {
                    if (!shouldRetry(result.error)) return result
                    delay(RETRY_DELAY_MS)
                }
            }
        }
    }

    private fun shouldRetry(error: AppError): Boolean {
        return when (error) {
            is AppError.Network -> {
                snackbarManager.show(UiText.Dynamic("Отсутствует подключение к интернету"))
                true
            }

            else -> false
        }
    }

    private data class Loaded(
        val user: User,
        val manager: List<QuizManagerUiModel>,
        val employee: List<QuizEmployeeUiModel>
    )

    private fun showUiError(error: AppError) {
        when (val uiError = error.toScreenUiError()) {
            is UiError.SnackBar -> snackbarManager.show(uiError.message)

            is UiError.FullScreen -> {
                val current = _state.value
                if (current is QuizUiState.Content) {
                    _state.value = current.copy(fullScreenError = uiError.model)
                } else {
                    _state.value = QuizUiState.Error(uiError.model)
                }
            }
        }
    }
}

sealed interface QuizCommand {
    data class InputSearchQuery(val query: String) : QuizCommand
    data class SelectStatus(val status: QuizStatus?) : QuizCommand
    data class DeleteQuiz(val quizId: Int) : QuizCommand
    data object Refresh : QuizCommand
}

sealed interface QuizUiState {
    data object Initial : QuizUiState
    data object Loading : QuizUiState
    data class Content(
        val user: User,
        val quizFilters: QuizFilters = QuizFilters(),
        val quizList: List<QuizManagerUiModel> = emptyList(),
        val assignmentsList: List<QuizEmployeeUiModel> = emptyList(),
        val isListLoading: Boolean = false,
        val fullScreenError: ErrorScreenModel? = null
    ) : QuizUiState

    data class Error(
        val model: ErrorScreenModel
    ) : QuizUiState
}