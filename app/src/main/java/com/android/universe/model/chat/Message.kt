package com.android.universe.model.chat

import com.google.firebase.Timestamp

data class Message(
    val senderId: String = "",
    val message: String = "",
    val timestamp: Timestamp = Timestamp.now()
)
