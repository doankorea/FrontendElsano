package com.vn.elsanobooking.data.models

import com.google.gson.annotations.SerializedName

data class LoginResponse(
    @SerializedName("Status") val status: String,
    @SerializedName("Message") val message: String? = null,
    @SerializedName("User") val user: User? = null,
    @SerializedName("Errors") val errors: List<String>? = null
) {
    data class User(
        @SerializedName("UserId") val userId: Int? = null,
        @SerializedName("UserName") val userName: String,
        @SerializedName("Email") val email: String,
        @SerializedName("Roles") val roles: List<String>? = null
    )
}