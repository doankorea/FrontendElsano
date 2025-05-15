package com.vn.elsanobooking.data.models

import com.google.gson.annotations.SerializedName

data class ResendEmailResponse (
    @SerializedName("Status") val status: String,
    @SerializedName("Message") val message: String?
)