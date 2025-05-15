package com.vn.elsanobooking.data.models

import com.google.gson.annotations.SerializedName

data class ResendEmailRequest(
    @SerializedName("Email") val email: String
) 