package com.i11usion.feature.main.presentation.quiz.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.i11usion.core.domain.model.quiz.QuizStatus
import com.i11usion.core.domain.model.user.Role
import com.i11usion.core.domain.model.user.User
import com.i11usion.core.domain.usecase.quiz.DeleteQuizUseCase
import com.i11usion.core.domain.usecase.quiz.GetMyQuizAssignmentsUseCase
import com.i11usion.core.domain.usecase.quiz.GetQuizListUseCase
import com.i11usion.core.domain.usecase.session.GetUserInfoUseCase
import com.i11usion.core.ui.formatter.formatInstantToStringData
import com.i11usion.core.ui.formatter.formatInstantToStringDataNullable
import com.i11usion.feature.main.presentation.quiz.list.mapper.quizToQuizEmployeeUi
import com.i11usion.feature.main.presentation.quiz.list.mapper.quizToQuizManagerUi
import com.i11usion.feature.main.presentation.quiz.list.model.QuizEmployeeUiModel
import com.i11usion.feature.main.presentation.quiz.list.model.QuizFilters
import com.i11usion.feature.main.presentation.quiz.list.model.QuizManagerUiModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
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
    private val deleteQuizUseCase: DeleteQuizUseCase
) : ViewModel() {
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
                    _state.value = QuizUiState.Error(
                        message = t.message ?: "Не удалось загрузить данные"
                    )
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
                    deleteQuizUseCase(command.quizId)
                    reloadQuizzes.emit(Unit)
                }
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
            .onEach { (user, _) ->
                _state.update { st ->
                    when (st) {
                        is QuizUiState.Content -> st.copy(isListLoading = true)
                        else -> QuizUiState.Content(
                            user = user,
                            quizFilters = QuizFilters(),
                            isListLoading = true
                        )
                    }
                }
            }
            .mapLatest { (user, currentFilters) ->
                runCatching {
                    val status = when (currentFilters.status) {
                        QuizStatus.PUBLISHED -> "published"
                        QuizStatus.DRAFT -> "draft"
                        null -> null
                    }

                    if (user.role == Role.MANAGER) {
                        val quizzes = getQuizListUseCase(
                            page = null,
                            perPage = null,
                            search = currentFilters.query,
                            status = status
                        )
                        val ui = quizzes.map { it.quizToQuizManagerUi(::formatInstantToStringData) }
                        Triple(user, ui, emptyList())
                    } else {
                        val assignments = getMyQuizAssignmentsUseCase()
                        val ui =
                            assignments.map { it.quizToQuizEmployeeUi(::formatInstantToStringDataNullable) }
                        Triple(user, emptyList<QuizManagerUiModel>(), ui)
                    }
                }
            }
            .onEach { result ->
                result.fold(
                    onSuccess = { (user, managerQuizzes, employeeAssignments) ->
                        _state.update { st ->
                            val uiFilters = (st as? QuizUiState.Content)?.quizFilters
                                ?: QuizFilters(
                                    query = filters.value.query,
                                    status = filters.value.status
                                )
                            QuizUiState.Content(
                                user = user,
                                quizFilters = uiFilters,
                                quizList = managerQuizzes,
                                assignmentsList = employeeAssignments,
                                isListLoading = false
                            )
                        }
                    },
                    onFailure = { t ->
                        _state.value = QuizUiState.Error(
                            message = t.message ?: "Не удалось загрузить тесты"
                        )
                    }
                )
            }
            .launchIn(viewModelScope)
    }
}

sealed interface QuizCommand {
    data class InputSearchQuery(val query: String) : QuizCommand
    data class SelectStatus(val status: QuizStatus?) : QuizCommand
    data class DeleteQuiz(val quizId: Int) : QuizCommand
}

sealed interface QuizUiState {
    data object Initial : QuizUiState
    data object Loading : QuizUiState
    data class Content(
        val user: User,
        val quizFilters: QuizFilters = QuizFilters(),
        val quizList: List<QuizManagerUiModel> = emptyList(),
        val assignmentsList: List<QuizEmployeeUiModel> = emptyList(),
        val isListLoading: Boolean = false
    ) : QuizUiState

    data class Error(
        val message: String
    ) : QuizUiState
}