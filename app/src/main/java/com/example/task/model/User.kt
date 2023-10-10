package com.example.task.model

data class User(
    val userId: String = "",
    val email: String = "",
    var isConnected: Boolean = false,  // Information about whether the user matches or not
    var connectedUserId: String = "",  // ID of the matching user
    var hasDisconnectedOnce: Boolean = false  // Has he ever left the user he is connected to?
)