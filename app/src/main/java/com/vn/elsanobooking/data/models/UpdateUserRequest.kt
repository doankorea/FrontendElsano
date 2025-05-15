package com.vn.elsanobooking.data.models

import com.google.gson.annotations.SerializedName

data class UpdateUserRequest(
    @SerializedName("UserName") val userName: String,
    @SerializedName("Email") val email: String,
    @SerializedName("PhoneNumber") val phoneNumber: String,
    @SerializedName("Avatar") val avatar: String?,
    @SerializedName("IsActive") val isActive: Int?
)