package com.i11usion.core.domain.model

data class MyQuizAssignment(
    val assignment: QuizAssignment,
    val quiz: Quiz,
    val task: Task?
)
