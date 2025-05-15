package com.vn.elsanobooking.data.models

data class SignalRChatMessage(
    val senderId: Int,
    val receiverId: Int,
    val content: String,
    val timestamp: String
) 