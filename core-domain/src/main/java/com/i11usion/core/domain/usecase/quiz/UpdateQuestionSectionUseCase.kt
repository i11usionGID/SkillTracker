package com.i11usion.core.domain.usecase.quiz

import com.i11usion.core.domain.repository.QuizRepository
import javax.inject.Inject

class UpdateQuestionSectionUseCase @Inject constructor(
    private val repository: QuizRepository
) {

    suspend operator fun invoke(
        quizId: Int,
        questionSectionId: Int,
        title: String,
        description: String? = null,
        orderIndex: Int,
        pickCount: Int?,
        questionIds: List<Int>
    ) {
        repository.updateQuestionSection(
            quizId = quizId,
            questionSectionId = questionSectionId,
            title = title,
            description = description,
            orderIndex = orderIndex,
            pickCount = pickCount,
            questionIds = questionIds
        )
    }
}