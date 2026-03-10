package com.i11usion.core.network.notification

import com.i11usion.core.network.model.notification.NotificationDto
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface NotificationService {

    @GET("/api/notifications")
    suspend fun getNotifications(
        @Query("unread_only") unreadOnly: Boolean?,
        @Query("limit") limit: Int?,
        @Query("offset") offset: Int?,
    ): List<NotificationDto>

    @POST("/api/notifications/{notification_id}/read")
    suspend fun readNotification(
        @Path("notification_id") notificationId: Int
    ): NotificationDto

    @POST("/api/notifications/read-all")
    suspend fun readAllNotification()
}