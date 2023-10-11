package com.example.task.model

data class User(
    val userId: String = "",
    val email: String = "",
    val token: String = "",
    var isConnected: Boolean = false,  // Information about whether the user matches or not
    var connectedUserId: String = "",  // ID of the matching user
    var isFollowing: Boolean = false,
    var followingUserId: String = ""
)