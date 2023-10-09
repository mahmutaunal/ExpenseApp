package com.example.task.model

data class User(
    val userId: String? = null,
    val email: String? = null,
    var isConnected: Boolean = false,
    val connectedUserId: String? = null,
    var hasDisconnectedOnce: Boolean = false
)