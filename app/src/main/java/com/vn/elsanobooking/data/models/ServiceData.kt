package com.vn.elsanobooking.data.models

import com.google.gson.annotations.SerializedName

data class ServiceData(
    @SerializedName("serviceDetailId") val serviceDetailId: Int,
    @SerializedName("serviceName") val serviceName: String,
    @SerializedName("price") val price: Double,
    @SerializedName("duration") val duration: Int
) 