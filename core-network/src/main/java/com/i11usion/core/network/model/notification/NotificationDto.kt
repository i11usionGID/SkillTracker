package com.i11usion.core.network.model.notification

import com.google.gson.JsonObject
import com.google.gson.annotations.SerializedName


data class NotificationDto(
    val id: Int,
    @SerializedName("user_id") val userId: Int,
    @SerializedName("type") val type: String,
    @SerializedName("title") val title: String,
    @SerializedName("body") val body: String? = null,
    @SerializedName("is_read") val isRead: Boolean,
    @SerializedName("created_at") val createdAt: String,
    val payload: JsonObject? = null
)

