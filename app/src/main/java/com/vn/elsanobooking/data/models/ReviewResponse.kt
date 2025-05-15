package com.vn.elsanobooking.data.models

data class ReviewResponse(
    val message: String,
    val review: Review
)

data class Review(
    val AppointmentId: Int,
    val UserId: Int,
    val ArtistId: Int,
    val Rating: Int,
    val Content: String,
    val CreatedAt: String
)