package com.vn.elsanobooking.data.models

data class MessagesUsersListViewModel(
    val id: Int,
    val userName: String,
    val avatar: String?,
    val lastMessage: String?,
    val lastMessageDate: String?,
    val unreadCount: Int = 0,
    val isArtist: Boolean = false,
    val username: String = "",
    val artist: ArtistInfo? = null
)