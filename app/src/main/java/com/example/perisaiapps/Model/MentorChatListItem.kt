package com.example.perisaiapps.Model

import com.google.firebase.Timestamp

data class MentorChatListItem(
    val chatRoomId: String = "",
    val menteeId: String = "",
    val menteeName: String = "",
    val menteePhotoUrl: String = "",
    val lastMessage: String = "",
    val lastActivityTimestamp: Timestamp = Timestamp.now(),
    val unreadCount: Int = 0
)