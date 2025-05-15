package com.vn.elsanobooking.data.models

import com.google.gson.annotations.SerializedName

data class RegisterResponse(
    @SerializedName("Status") val status: String,
    @SerializedName("Message") val message: String?,
    @SerializedName("UserId") val userId: Int?,
    @SerializedName("RequireEmailConfirmation") val requireEmailConfirmation: Boolean? = null,
    @SerializedName("Email") val email: String? = null
)