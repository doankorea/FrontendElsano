package com.vn.elsanobooking.config

import android.content.Context
import androidx.preference.PreferenceManager

object Constants {
    const val BASE_URL = "http://192.168.1.31:5207"
    const val CHAT_HUB_URL = "$BASE_URL/chatHub"
    
    // Auth token key for SharedPreferences
    const val AUTH_TOKEN_KEY = "auth_token"
    
    // Method to get the auth token from SharedPreferences
    var AUTH_TOKEN: String = ""
        private set
    
    // Call this method from the application startup or login screen
    fun updateAuthToken(context: Context) {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        AUTH_TOKEN = sharedPreferences.getString(AUTH_TOKEN_KEY, "") ?: ""
    }
    
    // Use this method to save the auth token during login
    fun saveAuthToken(context: Context, token: String) {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        sharedPreferences.edit().putString(AUTH_TOKEN_KEY, token).apply()
        AUTH_TOKEN = token
    }
}