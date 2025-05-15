package com.vn.elsanobooking

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.vn.elsanobooking.presentation.screen.MainScreen
import com.vn.elsanobooking.ui.theme.ElsanoBookingUITheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val splashScreen = installSplashScreen()

        splashScreen.setKeepOnScreenCondition {
            Thread.sleep(2000)
            false
        }
        enableEdgeToEdge()
        setContent {
            ElsanoBookingUITheme {
                MainScreen()
            }
        }
    }
}
