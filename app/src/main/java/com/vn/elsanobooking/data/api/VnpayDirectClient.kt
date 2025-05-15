package com.vn.elsanobooking.data.api

import android.util.Log
import com.google.gson.Gson
import com.vn.elsanobooking.config.Constants
import com.vn.elsanobooking.data.models.VnpayPaymentRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit

object VnpayDirectClient {
    private val client: OkHttpClient by lazy {
        val loggingInterceptor = okhttp3.logging.HttpLoggingInterceptor().apply {
            setLevel(okhttp3.logging.HttpLoggingInterceptor.Level.BODY)
        }
        
        OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }
    
    private val gson = Gson()
    
    /**
     * Directly call the VNPay API to get a payment URL
     */
    suspend fun getPaymentUrl(request: VnpayPaymentRequest): String? = withContext(Dispatchers.IO) {
        try {
            val baseUrl = Constants.BASE_URL.trim()
            val endpoint = if (baseUrl.endsWith("/")) "Payment/CreatePaymentUrlVnpay" else "/Payment/CreatePaymentUrlVnpay"
            val url = baseUrl + endpoint
            
            Log.d("VnpayDirectClient", "Base URL: $baseUrl")
            Log.d("VnpayDirectClient", "Calling direct URL: $url")
            
            val jsonRequest = gson.toJson(request)
            Log.d("VnpayDirectClient", "Request body: $jsonRequest")
            
            val requestBody = jsonRequest.toRequestBody("application/json".toMediaType())
            
            val httpRequest = Request.Builder()
                .url(url)
                .post(requestBody)
                .addHeader("Accept", "application/json")
                .addHeader("Content-Type", "application/json")
                .build()
                
            val response = client.newCall(httpRequest).execute()
            val responseBody = response.body?.string()
            
            Log.d("VnpayDirectClient", "Response: $responseBody")
            
            if (response.isSuccessful) {
                if (responseBody?.startsWith("http") == true) {
                    Log.d("VnpayDirectClient", "Returning direct URL from response")
                    return@withContext responseBody
                } 
                
                if (responseBody?.startsWith("{") == true) {
                    // Try to extract URL from JSON
                    try {
                        Log.d("VnpayDirectClient", "Trying to parse JSON response")
                        val jsonObject = gson.fromJson(responseBody, Map::class.java)
                        val paymentUrl = jsonObject["paymentUrl"] as? String
                        if (paymentUrl != null) {
                            Log.d("VnpayDirectClient", "Extracted URL from JSON: $paymentUrl")
                            return@withContext paymentUrl
                        } else {
                            // Kiểm tra các trường khác có thể chứa URL
                            Log.d("VnpayDirectClient", "JSON fields: ${jsonObject.keys.joinToString()}")
                            jsonObject.forEach { (key, value) ->
                                Log.d("VnpayDirectClient", "Field $key: $value")
                                if (value is String && value.startsWith("http")) {
                                    Log.d("VnpayDirectClient", "Found URL in field $key: $value")
                                    return@withContext value
                                }
                            }
                        }
                    } catch (e: Exception) {
                        Log.e("VnpayDirectClient", "Error parsing JSON: ${e.message}", e)
                        // Continue to other parsing methods
                    }
                }
                
                // Try to extract URL from HTML response
                if (responseBody?.contains("http") == true) {
                    Log.d("VnpayDirectClient", "Trying to extract URL from HTML content")
                    val urlRegex = "(https?://[^\\s\"'<>]+)".toRegex()
                    val match = urlRegex.find(responseBody)
                    if (match != null) {
                        val extractedUrl = match.value
                        Log.d("VnpayDirectClient", "Extracted URL from HTML: $extractedUrl")
                        return@withContext extractedUrl
                    }
                }
                
                Log.e("VnpayDirectClient", "Invalid response format: $responseBody")
                return@withContext null
            } else {
                Log.e("VnpayDirectClient", "HTTP Error ${response.code}: $responseBody")
                return@withContext null
            }
        } catch (e: Exception) {
            Log.e("VnpayDirectClient", "Error getting payment URL: ${e.message}", e)
            e.printStackTrace()
            return@withContext null
        }
    }
} 