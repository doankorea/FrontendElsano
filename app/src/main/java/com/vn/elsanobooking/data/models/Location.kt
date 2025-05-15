package com.vn.elsanobooking.data.models
import com.google.gson.annotations.SerializedName
data class Location(
    @SerializedName("id")
    val id: Int,
    @SerializedName("address")
    val address: String?,
    @SerializedName("latitude")
    val latitude: Double?,
    @SerializedName("longitude")
    val longitude: Double?,
    @SerializedName("type")
    val type: String?
)