package com.vn.elsanobooking.data.models

import com.google.gson.annotations.SerializedName
data class Artist(
    @SerializedName("id") val id: Int,
    @SerializedName("fullName") val fullName: String,
    @SerializedName("bio") val bio: String?,
    @SerializedName("specialty") val specialty: String?,
    @SerializedName("experience") val experience: String?,
    @SerializedName("isAvailableAtHome") val isAvailableAtHome: Boolean,
    @SerializedName("avatar") val avatar: String,
    @SerializedName("address") val address: String?,
    @SerializedName("latitude") val latitude: Double,
    @SerializedName("longitude") val longitude: Double,
    @SerializedName("rating") val rating: Double,
    @SerializedName("reviewsCount") val reviewsCount: Int,
    @SerializedName("location") val location: Location?,
    @SerializedName("appointments") val appointments: List<Appointment>,
    @SerializedName("services") var services: List<Service>?
)