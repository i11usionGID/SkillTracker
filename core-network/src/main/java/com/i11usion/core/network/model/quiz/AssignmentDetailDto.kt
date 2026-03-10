package com.i11usion.core.network.model.quiz

import com.google.gson.annotations.SerializedName
import com.i11usion.core.network.model.task.TaskDto
import com.i11usion.core.network.model.user.UserDto
import kotlinx.serialization.Serializable

@Serializable
data class AssignmentDetailDto(
    @SerializedName("assignment") val assignment: AssignmentDto,
    @SerializedName("quiz") val quiz: QuizDto,
    @SerializedName("task") val task: TaskDto? = null,
    @SerializedName("assigned_user") val assignedUser: UserDto? = null
)
