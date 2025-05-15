package com.vn.elsanobooking.data.models

import com.google.gson.annotations.SerializedName

data class BookAppointmentRequest(
    @SerializedName("artistId") val artistId: Int,
    @SerializedName("serviceId") val serviceId: Int,
    @SerializedName("serviceDetailId") val serviceDetailId: Int?,
    @SerializedName("startTime") val startTime: String,
    @SerializedName("endTime") val endTime: String,
    @SerializedName("meetingLocation") val meetingLocation: String,
    @SerializedName("userId") val userId: Int,
    @SerializedName("paymentMethod") val paymentMethod: String,
    @SerializedName("paymentUrl") val paymentUrl: String? = null
)