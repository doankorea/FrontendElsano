package com.vn.elsanobooking.data.models

import com.google.gson.annotations.SerializedName

data class Appointment(
    @SerializedName("appointmentId") val appointmentId: Int,
    @SerializedName("appointmentDate") val appointmentDate: String,
    @SerializedName("status") val status: String,
    @SerializedName("serviceDetailId") val serviceDetailId: Int
)