package com.vn.elsanobooking.presentation.components

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

/**
 * Component để xử lý quyền vị trí và lấy vị trí người dùng
 */
@Composable
fun LocationPermissionHandler(
    key: Int = 0, // Add a key parameter to trigger recomposition
    onLocationResult: (isGranted: Boolean, location: Location?) -> Unit
) {
    val context = LocalContext.current
    var showPermissionDialog by remember { mutableStateOf(false) }
    var showEnableLocationDialog by remember { mutableStateOf(false) }
    
    // Launcher để yêu cầu quyền truy cập vị trí
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val locationGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        
        if (locationGranted) {
            // Kiểm tra xem GPS đã bật chưa
            val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
            val isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
            
            if (isGpsEnabled) {
                // Lấy vị trí hiện tại nếu GPS đã bật
                getCurrentLocation(context) { location ->
                    onLocationResult(true, location)
                }
            } else {
                showEnableLocationDialog = true
                onLocationResult(false, null)
            }
        } else {
            showPermissionDialog = true
            onLocationResult(false, null)
        }
    }
    
    // Kiểm tra quyền và yêu cầu khi component được tạo hoặc key thay đổi
    LaunchedEffect(key) {
        checkAndRequestLocationPermission(context, permissionLauncher, onLocationResult)
    }
    
    // Dialog yêu cầu bật quyền vị trí
    if (showPermissionDialog) {
        AlertDialog(
            onDismissRequest = { showPermissionDialog = false },
            title = { Text("Yêu cầu quyền vị trí") },
            text = { 
                Column {
                    Text("Ứng dụng cần quyền truy cập vị trí để tìm thợ trang điểm gần bạn.")
                    Text(
                        "Vui lòng cấp quyền vị trí trong cài đặt ứng dụng.",
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showPermissionDialog = false
                        checkAndRequestLocationPermission(context, permissionLauncher, onLocationResult)
                    }
                ) {
                    Text("Cấp quyền")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPermissionDialog = false }) {
                    Text("Để sau")
                }
            }
        )
    }
    
    // Dialog yêu cầu bật GPS
    if (showEnableLocationDialog) {
        AlertDialog(
            onDismissRequest = { showEnableLocationDialog = false },
            title = { Text("Vui lòng bật GPS") },
            text = { Text("Cần bật GPS để chúng tôi có thể tìm thợ trang điểm gần bạn.") },
            confirmButton = {
                Button(
                    onClick = { 
                        showEnableLocationDialog = false
                        // Mở cài đặt vị trí của hệ thống
                        val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                        context.startActivity(intent)
                    }
                ) {
                    Text("Mở cài đặt")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEnableLocationDialog = false }) {
                    Text("Để sau")
                }
            }
        )
    }
}

/**
 * Kiểm tra và yêu cầu quyền truy cập vị trí nếu cần
 */
fun checkAndRequestLocationPermission(
    context: Context,
    permissionLauncher: androidx.activity.result.ActivityResultLauncher<Array<String>>,
    onLocationResult: (isGranted: Boolean, location: Location?) -> Unit
) {
    val fineLocationPermission = ContextCompat.checkSelfPermission(
        context, Manifest.permission.ACCESS_FINE_LOCATION
    )
    val coarseLocationPermission = ContextCompat.checkSelfPermission(
        context, Manifest.permission.ACCESS_COARSE_LOCATION
    )
    
    if (fineLocationPermission != PackageManager.PERMISSION_GRANTED &&
        coarseLocationPermission != PackageManager.PERMISSION_GRANTED) {
        // Yêu cầu cả hai quyền vị trí
        permissionLauncher.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    } else {
        // Đã có quyền, kiểm tra GPS
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        
        if (isGpsEnabled) {
            // If GPS is enabled and permission is granted, get current location
            getCurrentLocation(context) { location ->
                if (location != null) {
                    println("Location obtained: lat=${location.latitude}, lon=${location.longitude}")
                    onLocationResult(true, location)
                } else {
                    println("Failed to get current location")
                    onLocationResult(true, null)
                }
            }
        } else {
            // Nếu GPS chưa bật, hiển thị dialog nhắc nhở
            onLocationResult(false, null)
            return
        }
    }
}

/**
 * Lấy vị trí hiện tại của người dùng
 */
@SuppressLint("MissingPermission")
fun getCurrentLocation(
    context: Context,
    callback: (Location?) -> Unit
) {
    val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
    fusedLocationClient.lastLocation
        .addOnSuccessListener { location -> 
            callback(location)
        }
        .addOnFailureListener {
            callback(null)
        }
}

/**
 * Hàm suspend để lấy vị trí (có thể sử dụng trong coroutine)
 */
@SuppressLint("MissingPermission")
suspend fun getLastLocationSuspend(context: Context): Location? = suspendCancellableCoroutine { continuation ->
    val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
    fusedLocationClient.lastLocation
        .addOnSuccessListener { location ->
            continuation.resume(location)
        }
        .addOnFailureListener {
            continuation.resume(null)
        }
} 