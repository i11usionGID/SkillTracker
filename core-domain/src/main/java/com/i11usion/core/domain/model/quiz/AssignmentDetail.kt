package com.i11usion.core.domain.model.quiz

import com.i11usion.core.domain.model.task.Task
import com.i11usion.core.domain.model.user.User

data class AssignmentDetail(
    val assignment: Assignment,
    val quiz: Quiz,
    val task: Task?,
    val assignedUser: User?
)
