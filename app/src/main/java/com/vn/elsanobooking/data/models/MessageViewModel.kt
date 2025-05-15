package com.vn.elsanobooking.data.models

data class MessageViewModel(
    val currentUserId: Int,
    val receiverId: Int,
    val receiverUserName: String,
    val messages: List<MessageDetailsResponse>,
    val contact: ContactResponse
)

data class MessageDetailsResponse(
    val id: Int,
    val senderId: Int,
    val receiverId: Int,
    val content: String,
    val timestamp: String,
    val isRead: Boolean,
    val isSentByMe: Boolean
)

data class ContactResponse(
    val id: Int,
    val username: String,
    val avatar: String?,
    val isArtist: Boolean,
    val artist: ArtistInfo?
)

data class ArtistInfo(
    val id: Int,
    val fullName: String
)