package com.vn.elsanobooking.data.models

import com.google.gson.annotations.SerializedName


data class VnpayPaymentRequest(
    val appointmentId: Int,
    val orderType: String = "other",
    val amount: Double = 0.0,
    val orderDescription: String = "Thanh toán dịch vụ làm đẹp",
    val name: String = "Thanh toán dịch vụ"
)