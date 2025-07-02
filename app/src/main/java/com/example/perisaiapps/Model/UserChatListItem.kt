package com.example.perisaiapps.Model

import com.google.firebase.Timestamp

data class UserChatListItem(
    val chatRoomId: String = "",
    val mentorId: String = "",
    val mentorName: String = "",
    val mentorPhotoUrl: String = "",
    val lastMessage: String = "",
    val lastActivityTimestamp: Timestamp = Timestamp.now(),
    val unreadCount: Int = 0
)