package com.example.perisaiapps.Model

data class ChatConversation(
    val id: String,
    val menteeName: String,
    val menteePhotoUrl: String,
    val lastMessage: String,
    val timestamp: String,
    val unreadCount: Int = 0
)