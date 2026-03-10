package com.i11usion.core.domain.model.notification

enum class NotificationType {
    TASK_ASSIGNED,
    QUIZ_ASSIGNED,
    ATTEMPT_COMPLETED,
    COMMENT_ADDED,
    ATTACHMENT_ADDED,
    TASK_PROGRESS_UPDATED,
    QUIZ_AI_READY,
    QUIZ_AI_FAILED;

    companion object {

        fun fromApi(value: String): NotificationType = when (value.lowercase()) {
            "task_assigned" -> TASK_ASSIGNED
            "quiz_assigned" -> QUIZ_ASSIGNED
            "attempt_completed" -> ATTEMPT_COMPLETED
            "comment_added" -> COMMENT_ADDED
            "attachment_added" -> ATTACHMENT_ADDED
            "task_progress_updated" -> TASK_PROGRESS_UPDATED
            "quiz_ai_ready" -> QUIZ_AI_READY
            "quiz_ai_failed" -> QUIZ_AI_FAILED
            else -> error("Unknown MatcherType: $value")
        }
    }
}