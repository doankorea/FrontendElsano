package com.vn.elsanobooking.data.models

import com.google.gson.annotations.SerializedName

data class LocationData(
    @SerializedName("locationId") val locationId: Int,
    @SerializedName("address") val address: String,
    @SerializedName("latitude") val latitude: Double,
    @SerializedName("longitude") val longitude: Double
) 