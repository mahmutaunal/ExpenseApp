package com.example.task.model

data class Expense(
    var id: String = "",
    var category: String = "",
    var amount: Double = 0.0,
    var location: String? = null,
    val sharedWith: User? = null
)