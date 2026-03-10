package com.i11usion.core.domain.model.quiz.attempt

import com.i11usion.core.domain.model.quiz.Question

data class AttemptQuestion(
    val attemptQuestionId: Int,
    val question: Question,
    val sectionId: Int?,
    val orderIndex: Int
)
