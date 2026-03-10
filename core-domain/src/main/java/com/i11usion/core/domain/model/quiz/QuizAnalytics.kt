package com.i11usion.core.domain.model.quiz

data class QuizAnalytics(
    val quizId: Int,
    val totalAssignments: Int,
    val totalAttempts: Int,
    val completedAttempts: Int,
    val passRate: Double
)
