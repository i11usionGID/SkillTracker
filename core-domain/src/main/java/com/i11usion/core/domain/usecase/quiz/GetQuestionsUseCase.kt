package com.i11usion.core.domain.usecase.quiz

import com.i11usion.core.domain.model.network.AppResult
import com.i11usion.core.domain.model.quiz.Question
import com.i11usion.core.domain.model.quiz.QuestionType
import com.i11usion.core.domain.repository.QuizRepository
import javax.inject.Inject

class GetQuestionsUseCase @Inject constructor(
    private val repository: QuizRepository
) {

    suspend operator fun invoke(questionType: QuestionType? = null): AppResult<List<Question>> {
        return repository.getQuestions(questionType)
    }
}