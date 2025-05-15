package com.vn.elsanobooking.data.models

import com.google.gson.annotations.SerializedName

data class PaymentData(
    @SerializedName("paymentId") val paymentId: Int,
    @SerializedName("paymentMethod") val paymentMethod: String,
    @SerializedName("amount") val amount: Double,
    @SerializedName("paymentStatus") val paymentStatus: String,
    @SerializedName("createdAt") val createdAt: String
) 