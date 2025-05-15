package com.vn.elsanobooking.data.models

import com.google.gson.annotations.SerializedName

data class LocationResponse(
    @SerializedName("locationId") val locationId: Int,
    @SerializedName("latitude") val latitude: Double,
    @SerializedName("longitude") val longitude: Double,
    @SerializedName("address") val address: String,
    @SerializedName("type") val type: String
)