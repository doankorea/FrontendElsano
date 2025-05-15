package com.vn.elsanobooking.presentation.screen

import android.content.ContentValues.TAG
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import coil.compose.AsyncImage
import com.vn.elsanobooking.viewModel.AuthViewModel
import java.io.File
import java.io.FileOutputStream
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.saveable.rememberSaveable
import com.vn.elsanobooking.config.Constants
import com.vn.elsanobooking.data.api.Suggestion
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun ProfileScreen(navController: NavController, authViewModel: AuthViewModel) {
    var showEditDialog by remember { mutableStateOf(false) }
    var showAddLocationDialog by remember { mutableStateOf(false) }
    var avatarUri by remember { mutableStateOf<Uri?>(null) }
    val context = LocalContext.current

    // Launcher for picking an image
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            avatarUri = it
            val file = uri.toFile(context)
            file?.let {
                authViewModel.updateAvatar(it)
            } ?: Toast.makeText(context, "Không thể xử lý hình ảnh", Toast.LENGTH_SHORT).show()
        }
    }

    // Show toast for UI state
    LaunchedEffect(authViewModel.uiState) {
        when (val state = authViewModel.uiState) {
            is AuthViewModel.UiState.Success -> {
                Toast.makeText(context, state.message, Toast.LENGTH_SHORT).show()
            }
            is AuthViewModel.UiState.Error -> {
                Toast.makeText(context, state.message, Toast.LENGTH_LONG).show()
            }
            else -> {}
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F7FA))
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Header
        Text(
            "Hồ Sơ", 
            fontSize = 26.sp, 
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(20.dp))

        if (authViewModel.isLoading) {
            CircularProgressIndicator()
        } else {
            // Avatar with improved styling
            Card(
                modifier = Modifier
                    .size(120.dp),
                shape = CircleShape,
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.White)
                        .clickable { imagePickerLauncher.launch("image/*") },
                    contentAlignment = Alignment.Center
                ) {
                    authViewModel.avatar?.let { avatarUrl ->
                        AsyncImage(
                            model = "${Constants.BASE_URL}${avatarUrl}?t=${System.currentTimeMillis()}",
                            contentDescription = "Avatar",
                            modifier = Modifier.fillMaxSize()
                        )
                    } ?: Text(
                        "Thêm ảnh", 
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // User information card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    // User Information with dividers
                    ProfileInfoItem("ID", authViewModel.userId.toString())
                    Divider(modifier = Modifier.padding(vertical = 8.dp))
                    
                    ProfileInfoItem("Tên đăng nhập", authViewModel.userName)
                    Divider(modifier = Modifier.padding(vertical = 8.dp))
                    
                    ProfileInfoItem("Email", authViewModel.email)
                    Divider(modifier = Modifier.padding(vertical = 8.dp))
                    
                    authViewModel.phoneNumber.takeIf { it.isNotBlank() }
                        ?.let { ProfileInfoItem("Số điện thoại", it) }
                        ?: ProfileInfoItem("Số điện thoại", "Chưa cung cấp")
                    Divider(modifier = Modifier.padding(vertical = 8.dp))
                    
                    ProfileInfoItem(
                        "Trạng thái",
                        if (authViewModel.isActive == 1) "Hoạt động" else "Không hoạt động"
                    )
                    
                    authViewModel.location?.let { location ->
                        Divider(modifier = Modifier.padding(vertical = 8.dp))
                        ProfileInfoItem("Địa chỉ", location.address)
                        Divider(modifier = Modifier.padding(vertical = 8.dp))
                        ProfileInfoItem("Kinh độ", location.longitude.toString())
                        Divider(modifier = Modifier.padding(vertical = 8.dp))
                        ProfileInfoItem("Vĩ độ", location.latitude.toString())
                        Divider(modifier = Modifier.padding(vertical = 8.dp))
                        ProfileInfoItem("Loại", location.type)
                    } ?: run {
                        Divider(modifier = Modifier.padding(vertical = 8.dp))
                        ProfileInfoItem("Địa chỉ", "Chưa cung cấp")
                    }

                    authViewModel.createdAt?.let { 
                        Divider(modifier = Modifier.padding(vertical = 8.dp))
                        ProfileInfoItem("Tạo lúc", it) 
                    }
                    authViewModel.updatedAt?.let { 
                        Divider(modifier = Modifier.padding(vertical = 8.dp))
                        ProfileInfoItem("Cập nhật lúc", it) 
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Action Buttons
            Button(
                onClick = { showEditDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text(
                    "Chỉnh sửa thông tin",
                    fontSize = 16.sp
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = { showAddLocationDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            ) {
                Text(
                    "Thêm địa chỉ",
                    fontSize = 16.sp
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = {
                    authViewModel.logout()
                    navController.navigate("profile_screen") {
                        popUpTo(navController.graph.findStartDestination().id) { inclusive = true }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
            ) {
                Text(
                    "Đăng xuất", 
                    color = Color.White,
                    fontSize = 16.sp
                )
            }
        }
    }

    // Edit Profile Dialog
    if (showEditDialog) {
        EditProfileDialog(
            authViewModel = authViewModel,
            onDismiss = { showEditDialog = false }
        )
    }

    // Add Location Dialog
    if (showAddLocationDialog) {
        AddLocationDialog(
            authViewModel = authViewModel,
            onDismiss = { showAddLocationDialog = false }
        )
    }
}

@Composable
fun ProfileInfoItem(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            "$label:", 
            fontSize = 16.sp, 
            fontWeight = FontWeight.Medium,
            color = Color.Gray
        )
        Text(
            value, 
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
fun EditProfileDialog(authViewModel: AuthViewModel, onDismiss: () -> Unit) {
    var userName by remember { mutableStateOf(authViewModel.userName) }
    var email by remember { mutableStateOf(authViewModel.email) }
    var phoneNumber by remember { mutableStateOf(authViewModel.phoneNumber) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Chỉnh sửa thông tin", fontWeight = FontWeight.Bold) },
        text = {
            Column {
                OutlinedTextField(
                    value = userName,
                    onValueChange = { userName = it },
                    label = { Text("Tên đăng nhập") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = phoneNumber,
                    onValueChange = { phoneNumber = it },
                    label = { Text("Số điện thoại") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    authViewModel.updateUserInfo(
                        newUserName = userName,
                        newPhoneNumber = phoneNumber,
                        newEmail = email,
                        newAvatar = authViewModel.avatar,
                        newIsActive = authViewModel.isActive
                    )
                    onDismiss()
                },
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Lưu")
            }
        },
        dismissButton = {
            Button(
                onClick = onDismiss,
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.LightGray,
                    contentColor = Color.Black
                )
            ) {
                Text("Hủy")
            }
        }
    )
}

@Composable
fun AddLocationDialog(authViewModel: AuthViewModel, onDismiss: () -> Unit) {
    var address by rememberSaveable { mutableStateOf("") }
    var suggestions by remember { mutableStateOf<List<Suggestion>>(emptyList()) }
    var city by rememberSaveable { mutableStateOf("") }
    var district by rememberSaveable { mutableStateOf("") }
    var ward by rememberSaveable { mutableStateOf("") }
    var latitude by remember { mutableStateOf<Double?>(null) }
    var longitude by remember { mutableStateOf<Double?>(null) }
    var type by rememberSaveable { mutableStateOf("") }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val debounceSearch = remember {
        debounce<String>(300) { query ->
            if (query.length >= 2) {
                authViewModel.searchAddress(query) { result ->
                    suggestions = result
                }
            } else {
                suggestions = emptyList()
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Thêm địa chỉ") },
        text = {
            Column {
                // Trường nhập địa chỉ
                OutlinedTextField(
                    value = address,
                    onValueChange = {
                        address = it
                        debounceSearch(it) // Gọi tìm kiếm gợi ý
                    },
                    label = { Text("Địa chỉ") },
                    modifier = Modifier.fillMaxWidth()
                )

                // Danh sách gợi ý
                if (suggestions.isNotEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.White)
                            .clip(RoundedCornerShape(8.dp))
                            .verticalScroll(rememberScrollState())
                            .heightIn(max = 200.dp)
                    ) {
                        Column {
                            suggestions.forEach { suggestion ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp)
                                        .clickable {
                                            address = suggestion.description
                                            city = suggestion.compound?.province ?: ""
                                            district = suggestion.compound?.district ?: ""
                                            ward = suggestion.compound?.commune ?: ""
                                            // Gọi Geocoding API để lấy kinh độ, vĩ độ
                                            authViewModel.getCoordinates(suggestion.place_id) { lat, lng ->
                                                latitude = lat
                                                longitude = lng
                                            }
                                            suggestions = emptyList() // Ẩn gợi ý
                                        },
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("📍", modifier = Modifier.padding(end = 8.dp))
                                    Text(suggestion.description)
                                }
                                Divider()
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = city,
                    onValueChange = { city = it },
                    label = { Text("Tỉnh/Thành phố") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = district,
                    onValueChange = { district = it },
                    label = { Text("Quận/Huyện") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = ward,
                    onValueChange = { ward = it },
                    label = { Text("Phường/Xã") },
                    modifier = Modifier.fillMaxWidth()
                )
//                Spacer(modifier = Modifier.height(8.dp))
//                OutlinedTextField(
//                    value = type,
//                    onValueChange = { type = it },
//                    label = { Text("Loại") },
//                    modifier = Modifier.fillMaxWidth()
//                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (latitude != null && longitude != null && address.isNotBlank()) {
                        authViewModel.addLocation(
                            latitude = latitude!!,
                            longitude = longitude!!,
                            address = address,
                            type = type
                        )
                        onDismiss()
                    } else {
                        Toast.makeText(context, "Vui lòng chọn địa chỉ hợp lệ", Toast.LENGTH_SHORT).show()
                    }
                }
            ) {
                Text("Thêm")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Hủy")
            }
        }
    )
}

// Hàm debounce
fun <T> debounce(delayMs: Long, action: (T) -> Unit): (T) -> Unit {
    var lastJob: Job? = null
    return { param: T ->
        lastJob?.cancel()
        lastJob = CoroutineScope(Dispatchers.Main).launch {
            delay(delayMs)
            action(param)
        }
    }
}

fun Uri.toFile(context: android.content.Context): File? {
    return try {
        val inputStream = context.contentResolver.openInputStream(this)
        val file = File(context.cacheDir, "avatar_${System.currentTimeMillis()}.jpg")
        FileOutputStream(file).use { outputStream ->
            inputStream?.copyTo(outputStream) ?: throw IllegalStateException("Input stream is null")
        }
        file
    } catch (e: Exception) {
        Log.e(TAG, "Error converting Uri to File", e)
        null
    }
}