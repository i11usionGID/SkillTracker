package com.i11usion.feature.main.di

import androidx.lifecycle.ViewModel
import com.i11usion.di.ViewModelKey
import com.i11usion.feature.main.presentation.HomeViewModel
import com.i11usion.feature.main.presentation.dashboard.DashboardViewModel
import com.i11usion.feature.main.presentation.notification.NotificationViewModel
import com.i11usion.feature.main.presentation.quiz.create.CreateQuizViewModel
import com.i11usion.feature.main.presentation.quiz.detail.QuizDetailViewModel
import com.i11usion.feature.main.presentation.quiz.detail.assignment_section.CreateAssignmentViewModel
import com.i11usion.feature.main.presentation.quiz.detail.question_section.create.CreateQuestionSectionViewModel
import com.i11usion.feature.main.presentation.quiz.detail.question_section.update.UpdateQuestionSectionViewModel
import com.i11usion.feature.main.presentation.quiz.generate.GenerateQuizViewModel
import com.i11usion.feature.main.presentation.quiz.list.QuizViewModel
import com.i11usion.feature.main.presentation.quiz.question.create.CreateQuestionViewModel
import com.i11usion.feature.main.presentation.quiz.question.list.QuestionViewModel
import com.i11usion.feature.main.presentation.quiz.question.update.UpdateQuestionViewModel
import com.i11usion.feature.main.presentation.quiz.testing.analytics.detail.AttemptAnalyticsDetailViewModel
import com.i11usion.feature.main.presentation.quiz.testing.analytics.list.AttemptsAnalyticsViewModel
import com.i11usion.feature.main.presentation.quiz.testing.test.TestViewModel
import com.i11usion.feature.main.presentation.quiz.update.UpdateQuizViewModel
import com.i11usion.feature.main.presentation.task.create.CreateTaskViewModel
import com.i11usion.feature.main.presentation.task.detail.TaskDetailViewModel
import com.i11usion.feature.main.presentation.task.list.TaskViewModel
import com.i11usion.feature.main.presentation.user.create.CreateUserViewModel
import com.i11usion.feature.main.presentation.user.list.UserViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
interface MainViewModelModule {
    @Binds
    @IntoMap
    @ViewModelKey(HomeViewModel::class)
    fun bindHomeViewModel(vm: HomeViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(DashboardViewModel::class)
    fun bindDashboardViewModel(vm: DashboardViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(TaskViewModel::class)
    fun bindTaskViewModel(vm: TaskViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(UserViewModel::class)
    fun bindUserViewModel(vm: UserViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(QuizViewModel::class)
    fun bindQuizViewModel(vm: QuizViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(NotificationViewModel::class)
    fun bindNotificationViewModel(vm: NotificationViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(CreateQuizViewModel::class)
    fun bindCreateQuizViewModel(vm: CreateQuizViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(GenerateQuizViewModel::class)
    fun bindGenerateQuizViewModel(vm: GenerateQuizViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(QuizDetailViewModel::class)
    fun bindQuizDetailViewModel(vm: QuizDetailViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(TaskDetailViewModel::class)
    fun bindTaskDetailViewModel(vm: TaskDetailViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(CreateTaskViewModel::class)
    fun bindCreateTaskViewModel(vm: CreateTaskViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(CreateUserViewModel::class)
    fun bindCreateUserViewModel(vm: CreateUserViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(QuestionViewModel::class)
    fun bindQuestionViewModel(vm: QuestionViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(CreateQuestionViewModel::class)
    fun bindCreateQuestionViewModel(vm: CreateQuestionViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(UpdateQuestionViewModel::class)
    fun bindUpdateQuestionViewModel(vm: UpdateQuestionViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(TestViewModel::class)
    fun bindTestViewModel(vm: TestViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(CreateQuestionSectionViewModel::class)
    fun bindCreateQuestionSectionViewModel(vm: CreateQuestionSectionViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(UpdateQuestionSectionViewModel::class)
    fun bindUpdateQuestionSectionViewModel(vm: UpdateQuestionSectionViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(CreateAssignmentViewModel::class)
    fun bindCreateAssignmentViewModel(vm: CreateAssignmentViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(UpdateQuizViewModel::class)
    fun bindUpdateQuizViewModel(vm: UpdateQuizViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(AttemptsAnalyticsViewModel::class)
    fun bindAttemptsAnalyticsViewModel(vm: AttemptsAnalyticsViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(AttemptAnalyticsDetailViewModel::class)
    fun bindAttemptAnalyticsDetailViewModel(vm: AttemptAnalyticsDetailViewModel): ViewModel
}