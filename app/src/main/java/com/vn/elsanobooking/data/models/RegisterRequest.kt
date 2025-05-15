package com.vn.elsanobooking.data.models

import com.google.gson.annotations.SerializedName


data class RegisterRequest(
    val UserName: String,
    val Email: String,
    val Password: String
)