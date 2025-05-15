package com.vn.elsanobooking.data.api

import com.vn.elsanobooking.data.models.*
import okhttp3.MultipartBody
import retrofit2.http.*
import com.google.gson.annotations.SerializedName

interface AuthApi {
    @Headers("Content-Type: application/json")
    @POST("Account/CreateUser")
    suspend fun createUser(@Body request: RegisterRequest): RegisterResponse
    
    @POST("Account/LoginUser")
    suspend fun loginUser(@Body request: LoginRequest): LoginResponse
    
    @POST("Account/LogoutUser")
    suspend fun logoutUser(): LogoutResponse
    
    @POST("Account/ResendConfirmationEmail")
    suspend fun resendConfirmationEmail(@Body request: ResendEmailRequest): ResendEmailResponse

    @GET("api/user/{id}")
    suspend fun getUser(@Path("id") id: Int): UserResponse

    @PUT("api/user/{id}")
    suspend fun updateUser(@Path("id") id: Int, @Body request: UpdateUserRequest): UpdateUserResponse
    
    @Multipart
    @PUT("api/user/{id}/avatar")
    suspend fun updateAvatar(
        @Path("id") id: Int,
        @Part avatar: MultipartBody.Part
    ): UpdateAvatarResponse
    
    @POST("api/user/{id}/location")
    suspend fun addLocation(@Path("id") id: Int, @Body request: AddLocationRequest): AddLocationResponse
    
    @GET("api/user/search-address")
    suspend fun searchAddress(@Query("query") query: String): AutocompleteResponse

    @GET("api/user/geocode")
    suspend fun getCoordinates(@Query("placeId") placeId: String): CoordinateResponse
}
interface RatingApi {
    @POST("api/Review/rate")
    suspend fun submitRating(
        @Query("AppointmentId") appointmentId: Int,
        @Query("Rating") rating: Int,
        @Query("Comment") comment: String
    ): ReviewResponse
}
data class ApiResponse<T>(
    val status: String,
    val success: Boolean = status == "Success",
    val message: String? = null,
    val errors: List<String>? = null,
    val data: T? = null
)

interface MakeupArtistApi {
    @GET("api/artist/search")
    suspend fun searchArtists(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("radius") radius: Double = 10.0
    ): List<Artist>

    @GET("api/artist/{id}")
    suspend fun getArtistById(@Path("id") id: Int): Artist

    @POST("appointments")
    suspend fun bookAppointment(@Body request: BookAppointmentRequest): Unit
}

interface BookingApi {
    @POST("Apointment/Book")
    suspend fun bookAppointment(@Body request: BookAppointmentRequest): BookAppointmentResponse

    @GET("api/user/{userId}/appointments")
    suspend fun getUserAppointments(@Path("userId") userId: Int): ApiResponse<List<AppointmentResponse>>

    @PUT("api/appointments/{appointmentId}/cancel")
    suspend fun cancelAppointment(@Path("appointmentId") appointmentId: Int): ApiResponse<CancelAppointmentData>

    @GET("api/artist/{artistId}/appointments")
    suspend fun getArtistAppointments(@Path("artistId") artistId: Int): ApiResponse<List<AppointmentResponse>>
}

interface VnpayApi {
    @GET("Payment/PaymentCallbackVnpay")
    suspend fun checkPaymentCallback(@Query("appointmentId") appointmentId: Int): PaymentCallbackResponse
    
    @POST("Payment/CreatePaymentUrlVnpay")
    @Headers("Accept: application/json")
    suspend fun createVnpayUrl(@Body request: VnpayPaymentRequest): okhttp3.ResponseBody
}

interface ServiceApi {
    @GET("api/service/getall")
    suspend fun getAllServices(): List<ServiceApiModel>
}

data class AutocompleteResponse(
    val status: String,
    val predictions: List<Prediction>
)

data class Prediction(
    val description: String,
    val place_id: String,
    val compound: CompoundResponse?
)

data class CompoundResponse(
    val province: String?,
    val district: String?,
    val commune: String?
)

data class Suggestion(
    val description: String,
    val place_id: String,
    val compound: Compound?
)

data class Compound(
    val province: String?,
    val district: String?,
    val commune: String?
)

data class CoordinateResponse(
    val latitude: Double,
    val longitude: Double
)

data class CancelAppointmentResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String,
    @SerializedName("data") val data: CancelAppointmentData?
)

data class CancelAppointmentData(
    @SerializedName("appointmentId") val appointmentId: Int,
    @SerializedName("status") val status: String,
    @SerializedName("updatedAt") val updatedAt: String
)