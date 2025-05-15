package com.vn.elsanobooking

import android.app.Application
import android.content.Context
import com.vn.elsanobooking.config.Constants
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class ElsanoBookingApplication : Application() {
    
    override fun onCreate() {
        super.onCreate()
        appContext = applicationContext
        
        // Initialize auth token for SignalR
        Constants.updateAuthToken(applicationContext)
    }
    
    companion object {
        lateinit var appContext: Context
    }
} 