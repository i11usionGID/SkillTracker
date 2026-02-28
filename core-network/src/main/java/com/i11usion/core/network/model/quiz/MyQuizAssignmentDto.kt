package com.i11usion.core.network.model.quiz

import com.google.gson.annotations.SerializedName
import com.i11usion.core.network.model.task.TaskDto
import kotlinx.serialization.Serializable

@Serializable
data class MyQuizAssignmentDto(
    @SerializedName("assignment") val assignment: QuizAssignmentDto,
    @SerializedName("quiz") val quiz: QuizDto,
    @SerializedName("task") val task: TaskDto?
)
