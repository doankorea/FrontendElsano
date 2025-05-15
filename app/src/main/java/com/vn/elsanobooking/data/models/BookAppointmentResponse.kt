package com.vn.elsanobooking.data.models

import com.google.gson.annotations.SerializedName

data class BookAppointmentResponse(
    @SerializedName("status") val status: String,
    @SerializedName("success") val success: Boolean = status == "Success",
    @SerializedName("message") val message: String? = null,
    @SerializedName("data") val data: AppointmentData? = null
)

data class AppointmentData(
    @SerializedName("appointmentId") val appointmentId: Int,
    @SerializedName("message") val message: String? = null
) 