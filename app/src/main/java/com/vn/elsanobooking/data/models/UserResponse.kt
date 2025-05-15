package com.vn.elsanobooking.data.models

import com.google.gson.annotations.SerializedName
import java.time.OffsetDateTime

data class UserResponse(
    @SerializedName("id") val id: Int,
    @SerializedName("userName") val userName: String,
    @SerializedName("email") val email: String,
    @SerializedName("phoneNumber") val phoneNumber: String?,
    @SerializedName("avatar") val avatar: String?,
    @SerializedName("isActive") val isActive: Int,
    @SerializedName("locationId") val locationId: Int?,
    @SerializedName("location") val location: LocationResponse?,
    val createdAt: String?,
    val updatedAt: String?
)

