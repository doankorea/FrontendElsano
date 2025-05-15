package com.vn.elsanobooking.data.models

data class MessagesResponse(
    val success: Boolean,
    val data: List<MessagesUsersListViewModel>,
    val message: String?
) 