package com.example.feastfast.model

data class ChatMessage(
    val senderId: String? = null,
    val senderName: String? = null,
    val message: String? = null,
    val timestamp: Long = 0,
    val sentByAdmin: Boolean = false // To differentiate between user and admin messages
) {
    // Add an empty constructor for Firebase deserialization
    constructor() : this("", "", "", 0, false)
}
