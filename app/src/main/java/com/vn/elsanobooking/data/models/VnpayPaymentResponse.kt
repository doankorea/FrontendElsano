package com.vn.elsanobooking.data.models

import com.google.gson.annotations.SerializedName

data class VnpayPaymentResponse(
    @SerializedName("success") val success: Boolean = true,
    @SerializedName("paymentUrl") val paymentUrl: String
)