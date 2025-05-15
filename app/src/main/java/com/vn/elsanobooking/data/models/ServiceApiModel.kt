package com.vn.elsanobooking.data.models
data class ServiceApiModel(
    val id: Int,
    val name: String,
    val description: String,
    val imageUrl: String,
    val isActive: Boolean,
    val createdAt: String
)