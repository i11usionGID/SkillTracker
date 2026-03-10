package com.i11usion.core.domain.model.quiz

import com.i11usion.core.domain.model.task.Task

data class MyQuizAssignment(
    val assignment: QuizAssignment,
    val quiz: Quiz,
    val task: Task?
)
