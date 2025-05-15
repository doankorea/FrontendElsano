package com.vn.elsanobooking.data.models
import com.google.gson.annotations.SerializedName
data class Service(
    @SerializedName("serviceId") val serviceId: Int,
    @SerializedName("serviceName") val serviceName: String,
    @SerializedName("price") val price: Double,
    @SerializedName("duration") val duration: Int,
    @SerializedName("serviceDetailId") val serviceDetailId: Int? = null
)