package com.vn.elsanobooking.data.api

import com.google.firebase.appdistribution.gradle.ApiService
import com.google.gson.GsonBuilder
import com.vn.elsanobooking.config.Constants
import com.vn.elsanobooking.config.Constants.BASE_URL
import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import java.util.concurrent.TimeUnit
import android.util.Log

object RetrofitInstance {

    private val cookieJar = object : CookieJar {
        private val cookieStore = mutableMapOf<String, List<Cookie>>()

        override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
            cookieStore[url.host] = cookies
        }

        override fun loadForRequest(url: HttpUrl): List<Cookie> {
            return cookieStore[url.host] ?: emptyList()
        }
    }

    private val client: OkHttpClient by lazy {
        val logging = HttpLoggingInterceptor().apply {
            setLevel(HttpLoggingInterceptor.Level.BODY)
        }
        OkHttpClient.Builder()
            .addInterceptor(logging)
            .addInterceptor { chain ->
                val originalRequest = chain.request()
                val response = chain.proceed(originalRequest)
                
                // Check if the response is successful
                if (!response.isSuccessful) {
                    Log.e("RetrofitError", "Error code: ${response.code}, URL: ${originalRequest.url}")
                    
                    // Không đọc body ở đây vì sẽ gây ra lỗi "closed"
                    // Chỉ log thông tin cơ bản về response
                }
                
                response
            }
            .cookieJar(cookieJar)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    // Configure Gson for proper handling of Unicode characters
    private val gson = GsonBuilder()
        .setLenient()
        .disableHtmlEscaping() // This ensures Unicode characters are not escaped
        .create()

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            // Important: Add ScalarsConverterFactory first for String responses
            .addConverterFactory(ScalarsConverterFactory.create())
            // Then add GsonConverterFactory for JSON responses
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    val authApi: AuthApi by lazy {
        retrofit.create(AuthApi::class.java)
    }

    val generalApi: ServiceApi by lazy {
        retrofit.create(ServiceApi::class.java)
    }

    val messageApi: MessageApiService by lazy {
        retrofit.create(MessageApiService::class.java)
    }

    val makeupArtistApi: MakeupArtistApi by lazy {
        retrofit.create(MakeupArtistApi::class.java)
    }

    val bookingApi: BookingApi by lazy {
        retrofit.create(BookingApi::class.java)
    }

    val vnpayApi: VnpayApi by lazy {
        retrofit.create(VnpayApi::class.java)
    }

    val ratingApi: RatingApi by lazy {
        retrofit.create(RatingApi::class.java)
    }
}