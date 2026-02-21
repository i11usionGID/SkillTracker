package com.i11usion.core.domain.usecase.notification

import com.i11usion.core.domain.repository.NotificationRepository
import javax.inject.Inject

class ReadAllNotificationsUseCase @Inject constructor(
    private val repository: NotificationRepository
){

    suspend operator fun invoke() {
        repository.readAllNotifications()
    }
}