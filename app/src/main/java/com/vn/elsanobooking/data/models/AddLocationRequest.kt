package com.vn.elsanobooking.data.models

import com.google.gson.annotations.SerializedName

data class AddLocationRequest(
    @SerializedName("Latitude") val latitude: Double,
    @SerializedName("Longitude") val longitude: Double,
    @SerializedName("Address") val address: String,
    @SerializedName("Type") val type: String
)