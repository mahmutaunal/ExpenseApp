package com.example.task.model.notification

// Contains the data to be used in the notification.
data class PushNotification(
    val data: NotificationData,
    val to: String
)