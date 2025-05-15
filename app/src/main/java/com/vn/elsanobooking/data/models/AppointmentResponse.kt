package com.vn.elsanobooking.data.models

import com.google.gson.annotations.SerializedName

data class AppointmentResponse(
    @SerializedName("appointmentId") val appointmentId: Int,
    @SerializedName("userId") val userId: Int,
    @SerializedName("artistId") val artistId: Int,
    @SerializedName("artistName") val artistName: String,
    @SerializedName("appointmentDate") val appointmentDate: String,
    @SerializedName("status") val status: String,
    @SerializedName("createdAt") val createdAt: String,
    @SerializedName("updatedAt") val updatedAt: String,
    @SerializedName("service") val service: ServiceData,
    @SerializedName("location") val location: LocationData,
    @SerializedName("payment") val payment: PaymentData?
) 