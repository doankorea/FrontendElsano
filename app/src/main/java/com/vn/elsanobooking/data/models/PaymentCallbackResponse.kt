package com.vn.elsanobooking.data.models

import com.google.gson.annotations.SerializedName

data class PaymentCallbackResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("data") val data: PaymentCallbackData? = null,
    @SerializedName("message") val message: String? = null
)

data class PaymentCallbackData(
    @SerializedName("appointmentId") val appointmentId: Int,
    @SerializedName("paymentStatus") val paymentStatus: String,
    @SerializedName("appointmentStatus") val appointmentStatus: String,
    @SerializedName("message") val message: String
) 