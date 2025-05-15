package com.vn.elsanobooking.presentation.screen

import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import coil.compose.AsyncImage
import com.vn.elsanobooking.R
import com.vn.elsanobooking.config.Constants
import com.vn.elsanobooking.data.api.RetrofitInstance
import com.vn.elsanobooking.data.models.Artist
import com.vn.elsanobooking.data.models.Service
import com.vn.elsanobooking.ui.theme.BluePrimary
import com.vn.elsanobooking.viewModel.MakeupArtistViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.saveable.rememberSaveable
import com.vn.elsanobooking.data.models.BookAppointmentRequest
import com.vn.elsanobooking.data.models.BookAppointmentResponse
import com.vn.elsanobooking.data.models.AppointmentData
import kotlinx.coroutines.delay
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.lifecycle.viewmodel.compose.viewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArtistDetailScreen(
    artistId: Int,
    userId: Int,
    onBackClick: () -> Unit = {},
    onNavigateToSchedule: () -> Unit = {},
    onNavigateToChat: (Int) -> Unit = {}
) {
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    var userAddress by remember { mutableStateOf<String?>(null) }
    var userName by remember { mutableStateOf<String?>(null) }
    val viewModel: MakeupArtistViewModel= viewModel();
    // Lưu service ID và serviceDetailId
    var selectedServiceId by rememberSaveable { mutableStateOf<Int?>(null) }
    var selectedServiceDetailId by rememberSaveable { mutableStateOf<Int?>(null) }

    val selectedService = selectedServiceId?.let { id ->
        viewModel.services.find { it.serviceId == id }
    }

    var selectedStartTime by rememberSaveable { mutableStateOf<Date?>(null) }
    var selectedEndTime by rememberSaveable { mutableStateOf<Date?>(null) }
    var selectedMeetingLocation by rememberSaveable { mutableStateOf<String?>(null) }
    var showDateTimePicker by remember { mutableStateOf(false) }
    var appointmentId by rememberSaveable { mutableStateOf<Int?>(null) }

    // Dialog state variables
    var showDialog by remember { mutableStateOf(false) }
    var dialogTitle by remember { mutableStateOf("") }
    var dialogMessage by remember { mutableStateOf("") }
    var isPaymentSuccess by remember { mutableStateOf(false) }
    var bookSuccess by remember { mutableStateOf(false) }

    val artist = viewModel.selectedArtist
    val services = viewModel.services
    val isLoading = viewModel.isLoading
    val uiState = viewModel.uiState

    // Handle successful booking
    LaunchedEffect(bookSuccess) {
        if (bookSuccess) {
            showDialog = true
            dialogTitle = "Đặt lịch thành công"
            dialogMessage = "Lịch hẹn của bạn đã được đặt thành công. Bạn có thể xem chi tiết trong mục Lịch hẹn."

            // Add delay before navigating to schedule screen
            delay(2000)
            onNavigateToSchedule()
        }
    }

    // Show dialog if needed
    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text(text = dialogTitle, fontWeight = FontWeight.Bold) },
            text = { 
                Column {
                    Text(text = dialogMessage)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Bạn có thể xem chi tiết trong mục Lịch hẹn.", 
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF4CAF50)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDialog = false
                        if (bookSuccess) {
                            onNavigateToSchedule()
                        }
                    }
                ) {
                    Text("Đi đến Lịch hẹn")
                }
            }
        )
    }

    LaunchedEffect(artistId, userId) {
        coroutineScope.launch {
            try {
                viewModel.isLoading = true
                viewModel.getArtistDetails(artistId)
                val userResponse = RetrofitInstance.authApi.getUser(userId)
                userAddress = userResponse.location?.address
                userName = userResponse.userName ?: "Unknown User"
            } catch (e: Exception) {
                Log.e("ArtistDetailScreen", "Lỗi tải dữ liệu: ${e.message}", e)
                viewModel.uiState = MakeupArtistViewModel.UiState.Error("Lỗi khi tải dữ liệu: ${e.message}")
            } finally {
                viewModel.isLoading = false
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Chi tiết thợ trang điểm", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_arrow_back),
                            contentDescription = "Back"
                        )
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF5F5F5))
                .padding(padding),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                when {
                    isLoading -> {
                        CircularProgressIndicator(modifier = Modifier.padding(16.dp))
                    }

                    uiState is MakeupArtistViewModel.UiState.Error -> {
                        Text(
                            text = (uiState as MakeupArtistViewModel.UiState.Error).message,
                            color = Color.Red,
                            modifier = Modifier.padding(16.dp)
                        )
                    }

                    artist != null -> {
                        val artistData = artist!!

                        ArtistInfoSection(artistData, onNavigateToChat)
                        TextSection("Giới thiệu", artistData.bio ?: "Chưa có thông tin giới thiệu")
                        TextSection("Chuyên môn", artistData.specialty ?: "Không xác định")
                        TextSection("Kinh nghiệm", artistData.experience ?: "Không xác định")
                        ServiceDropdownSection(services, selectedService) { service ->
                            Log.d("ArtistDetailScreen", "Service selected: ID=${service.serviceId}, Name=${service.serviceName}")
                            selectedServiceId = service.serviceId
                            selectedServiceDetailId = service.serviceDetailId

                            // Debug log chi tiết về service được chọn
                            Log.d("ServiceDebug", "Service được chọn: ")
                            Log.d("ServiceDebug", "- serviceId: ${service.serviceId}")
                            Log.d("ServiceDebug", "- serviceDetailId: ${service.serviceDetailId}")
                            Log.d("ServiceDebug", "- serviceName: ${service.serviceName}")
                            Log.d("ServiceDebug", "- price: ${service.price}")
                            Log.d("ServiceDebug", "- duration: ${service.duration}")
                        }
                        TimeSelectionSection(
                            selectedService,
                            selectedStartTime,
                            selectedEndTime,
                            onSelectStartTime = { selectedStartTime = it },
                            onSelectEndTime = { selectedEndTime = it },
                            onShowDateTimePicker = { showDateTimePicker = true }
                        )
                        MeetingLocationDropdownSection(
                            artistData.address,
                            userAddress,
                            artistData.isAvailableAtHome,
                            selectedMeetingLocation
                        ) { selectedMeetingLocation = it }

                        Button(
                            onClick = {
                                // Debug log chi tiết để kiểm tra giá trị các biến
                                Log.d("ArtistDetailScreen", "Xác nhận đặt lịch - Debug: ")
                                Log.d("ArtistDetailScreen", "Service=${selectedService?.serviceId}-${selectedService?.serviceName}")
                                Log.d("ArtistDetailScreen", "StartTime=${selectedStartTime}")
                                Log.d("ArtistDetailScreen", "EndTime=${selectedEndTime}")
                                Log.d("ArtistDetailScreen", "Location=${selectedMeetingLocation}")

                                if (selectedService == null || selectedStartTime == null ||
                                    selectedEndTime == null || selectedMeetingLocation == null) {
                                    // Xác định chính xác trường thông tin nào đang thiếu
                                    val missingFields = mutableListOf<String>().apply {
                                        if (selectedService == null) add("dịch vụ")
                                        if (selectedStartTime == null || selectedEndTime == null) add("thời gian")
                                        if (selectedMeetingLocation == null) add("địa điểm")
                                    }

                                    val errorMessage = "Vui lòng chọn đầy đủ: ${missingFields.joinToString(", ")}"
                                    viewModel.uiState = MakeupArtistViewModel.UiState.Error(errorMessage)

                                    coroutineScope.launch {
                                        snackbarHostState.showSnackbar(
                                            message = errorMessage,
                                            duration = SnackbarDuration.Long
                                        )
                                    }
                                } else {
                                    coroutineScope.launch {
                                        try {
                                            viewModel.isLoading = true
                                            val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())

                                            // Xác định ID nào sẽ được sử dụng để đặt lịch
                                            // Cả hai ID đều phải được gửi, theo yêu cầu của backend
                                            val finalServiceId: Int = selectedServiceId ?: 0
                                            // serviceDetailId có thể để null nếu không có
                                            val finalServiceDetailId: Int? = selectedServiceDetailId

                                            // Log debug trước khi tạo request
                                            Log.d("ArtistDetailScreen", "Tạo request với serviceId=$finalServiceId, serviceDetailId=$finalServiceDetailId")

                                            val bookRequest = BookAppointmentRequest(
                                                artistId = artistId,
                                                serviceId = finalServiceId,
                                                serviceDetailId = finalServiceDetailId,
                                                startTime = dateFormat.format(selectedStartTime!!),
                                                endTime = dateFormat.format(selectedEndTime!!),
                                                meetingLocation = selectedMeetingLocation!!,
                                                userId = userId,
                                                paymentMethod = "Online"
                                            )

                                            // Log chi tiết về request gửi đi
                                            Log.d("BookRequestDebug", "---------- BOOKING REQUEST ----------")
                                            Log.d("BookRequestDebug", "Artist ID: $artistId")
                                            Log.d("BookRequestDebug", "Service ID: ${selectedServiceId}")
                                            Log.d("BookRequestDebug", "Service Detail ID: ${selectedServiceDetailId}")
                                            Log.d("BookRequestDebug", "Service Details - Name: ${selectedService?.serviceName}, Price: ${selectedService?.price}")
                                            Log.d("BookRequestDebug", "Start Time: ${dateFormat.format(selectedStartTime!!)}")
                                            Log.d("BookRequestDebug", "End Time: ${dateFormat.format(selectedEndTime!!)}")
                                            Log.d("BookRequestDebug", "Meeting Location: $selectedMeetingLocation")
                                            Log.d("BookRequestDebug", "User ID: $userId")
                                            Log.d("BookRequestDebug", "Payment Method: Online")
                                            Log.d("BookRequestDebug", "-----------------------------------")

                                            Log.d("ArtistDetailScreen", "Gửi request đặt lịch: $bookRequest")
                                            val bookResponse = RetrofitInstance.bookingApi.bookAppointment(bookRequest)
                                            Log.d("ArtistDetailScreen", "Phản hồi đặt lịch: $bookResponse")
                                            appointmentId = bookResponse.data?.appointmentId ?: -1

                                            // Debug log để kiểm tra URL thanh toán và giá dịch vụ
                                            Log.d("BookingDebug", "Selected Service: ID=${selectedServiceId}, Name=${selectedService?.serviceName}, Price=${selectedService?.price}")
                                            
                                            // Hiện thông báo thành công và dẫn hướng sang màn hình Lịch hẹn
                                            viewModel.uiState = MakeupArtistViewModel.UiState.Success(
                                                "Đặt lịch thành công. Xem chi tiết trong mục Lịch hẹn."
                                            )

                                            // Set bookSuccess to true to trigger navigation
                                            bookSuccess = true

                                            coroutineScope.launch {
                                                snackbarHostState.showSnackbar(
                                                    message = "Đặt lịch thành công. Xem chi tiết trong mục Lịch hẹn.",
                                                    duration = SnackbarDuration.Short
                                                )
                                            }
                                        } catch (e: Exception) {
                                            Log.e("ArtistDetailScreen", "Lỗi đặt lịch: ${e.message}", e)

                                            // Xử lý chi tiết hơn cho lỗi HTTP
                                            if (e is retrofit2.HttpException) {
                                                try {
                                                    // Lấy body của response lỗi
                                                    val errorBody = e.response()?.errorBody()?.string()
                                                    Log.e("ArtistDetailScreen", "Chi tiết lỗi từ server: $errorBody")

                                                    // Hiển thị thông báo lỗi cụ thể hơn
                                                    viewModel.uiState = MakeupArtistViewModel.UiState.Error(
                                                        "Lỗi khi đặt lịch: ${e.message()} - Chi tiết: $errorBody"
                                                    )

                                                    coroutineScope.launch {
                                                        snackbarHostState.showSnackbar(
                                                            message = "Lỗi đặt lịch: Vui lòng kiểm tra lại thông tin",
                                                            duration = SnackbarDuration.Short
                                                        )
                                                    }
                                                } catch (parseEx: Exception) {
                                                    Log.e("ArtistDetailScreen", "Lỗi khi phân tích response lỗi: ${parseEx.message}")
                                                    viewModel.uiState = MakeupArtistViewModel.UiState.Error(
                                                        "Lỗi khi đặt lịch: ${e.message()}"
                                                    )
                                                    coroutineScope.launch {
                                                        snackbarHostState.showSnackbar(
                                                            message = "Lỗi khi đặt lịch: ${e.message()}",
                                                            duration = SnackbarDuration.Short
                                                        )
                                                    }
                                                }
                                            } else {
                                                // Xử lý các lỗi khác
                                                viewModel.uiState = MakeupArtistViewModel.UiState.Error(
                                                    "Lỗi khi đặt lịch: ${e.message}"
                                                )
                                                coroutineScope.launch {
                                                    snackbarHostState.showSnackbar(
                                                        message = "Lỗi khi đặt lịch: ${e.message}",
                                                        duration = SnackbarDuration.Short
                                                    )
                                                }
                                            }
                                        } finally {
                                            viewModel.isLoading = false
                                        }
                                    }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .padding(top = 8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = BluePrimary),
                            enabled = !isLoading
                        ) {
                            Text("Xác nhận đặt lịch", color = Color.White, fontSize = 16.sp)
                        }
                    }

                    else -> {
                        Text(
                            text = "Không tìm thấy thông tin thợ trang điểm",
                            color = Color.Gray,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
            }
        }
    }

    if (showDateTimePicker) {
        DateTimePickerDialog(
            onDismiss = { showDateTimePicker = false },
            onConfirm = { start, end ->
                selectedStartTime = start
                selectedEndTime = end
                showDateTimePicker = false
            },
            duration = selectedService?.duration ?: 30
        )
    }
}

@Composable
fun ArtistInfoSection(
    artist: Artist,
    onNavigateToChat: (Int) -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = artist.avatar.takeIf { it.isNotBlank() }
                    ?.let { "${Constants.BASE_URL}$it?t=${System.currentTimeMillis()}" } ?: "",
                contentDescription = "${artist.fullName}'s avatar",
                modifier = Modifier
                    .size(64.dp)
                    .background(Color.Gray, RoundedCornerShape(32.dp)),
                placeholder = painterResource(id = android.R.drawable.ic_menu_gallery)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(artist.fullName, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Text("Đánh giá: ${artist.rating} (${artist.reviewsCount} lượt)", fontSize = 14.sp, color = Color.Gray)
                Text(artist.address ?: "", fontSize = 14.sp, color = Color.Gray)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Nút xem chỉ đường
        val context = LocalContext.current
        Button(
            onClick = {
                if (artist.latitude != 0.0 && artist.longitude != 0.0) {
                    // Mở Google Maps với chỉ đường từ vị trí hiện tại đến vị trí của artist
                    val uri = Uri.parse("google.navigation:q=${artist.latitude},${artist.longitude}")
                    val mapIntent = Intent(Intent.ACTION_VIEW, uri)
                    mapIntent.setPackage("com.google.android.apps.maps")

                    // Kiểm tra xem thiết bị có ứng dụng Maps không
                    if (mapIntent.resolveActivity(context.packageManager) != null) {
                        context.startActivity(mapIntent)
                    } else {
                        // Fallback nếu không có ứng dụng Maps
                        val browserUri = Uri.parse("https://www.google.com/maps/dir/?api=1&destination=${artist.latitude},${artist.longitude}")
                        val browserIntent = Intent(Intent.ACTION_VIEW, browserUri)
                        context.startActivity(browserIntent)
                    }
                } else {
                    // Hiển thị thông báo nếu không có tọa độ
                    Toast.makeText(context, "Không có thông tin vị trí của artist", Toast.LENGTH_SHORT).show()
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(42.dp),
            colors = ButtonDefaults.buttonColors(containerColor = BluePrimary)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_directions),
                contentDescription = "Directions",
                tint = Color.White,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Xem chỉ đường đến địa điểm", color = Color.White)
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Nút nhắn tin với artist
        OutlinedButton(
            onClick = { onNavigateToChat(artist.id) },
            modifier = Modifier
                .fillMaxWidth()
                .height(42.dp),
            border = BorderStroke(1.dp, Color(0xFF4CAF50)),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF4CAF50))
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_bottom_chat),
                contentDescription = "Chat",
                tint = Color(0xFF4CAF50),
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Nhắn tin với ${artist.fullName.split(" ").lastOrNull() ?: "artist"}", color = Color(0xFF4CAF50))
        }
    }
}

@Composable
fun TextSection(title: String, value: String) {
    Text(title, fontSize = 16.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(vertical = 8.dp))
    Text(value, fontSize = 14.sp, color = Color.Gray)
}

@Composable
fun ServiceDropdownSection(
    services: List<Service>,
    selected: Service?,
    onSelect: (Service) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Text(
        "Dịch vụ cung cấp",
        fontSize = 16.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(vertical = 8.dp)
    )

    Box {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = true },
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    selected?.serviceName ?: "Chọn dịch vụ",
                    fontSize = 14.sp,
                    color = if (selected != null) Color.Black else Color.Gray
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    selected?.let { "${it.price} VNĐ" } ?: "",
                    fontSize = 14.sp,
                    color = Color.Black
                )
            }
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .zIndex(1f)
        ) {
            services.forEach { service ->
                Log.d("ServiceDebug", "Service in dropdown: ID=${service.serviceId}, Name=${service.serviceName}")
                DropdownMenuItem(
                    text = {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(service.serviceName ?: "Dịch vụ không tên")
                            Text("${service.price} VNĐ")
                        }
                    },
                    onClick = {
                        Log.d("ServiceDebug", "Selected service: ID=${service.serviceId}, Name=${service.serviceName}")
                        onSelect(service)
                        expanded = false
                    },
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                )
            }
        }
    }
}

@Composable
fun TimeSelectionSection(
    selectedService: Service?,
    selectedStartTime: Date?,
    selectedEndTime: Date?,
    onSelectStartTime: (Date) -> Unit,
    onSelectEndTime: (Date) -> Unit,
    onShowDateTimePicker: () -> Unit
) {
    val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    Text("Chọn thời gian", fontSize = 16.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(vertical = 8.dp))
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onShowDateTimePicker() },
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = selectedStartTime?.let { dateFormat.format(it) } ?: "Chọn thời gian bắt đầu",
                fontSize = 14.sp,
                color = if (selectedStartTime != null) Color.Black else Color.Gray
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = selectedEndTime?.let { dateFormat.format(it) } ?: "Thời gian kết thúc",
                fontSize = 14.sp,
                color = if (selectedEndTime != null) Color.Black else Color.Gray
            )
        }
    }
}

@Composable
fun MeetingLocationDropdownSection(
    artistAddress: String?,
    userAddress: String?,
    isAvailableAtHome: Boolean?,
    selected: String?,
    onSelect: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val availableLocations = mutableListOf<String>().apply {
        artistAddress?.let { add(it) }
        if (isAvailableAtHome == true) {
            userAddress?.let { add(it) }
        }
    }

    Text(
        "Chọn địa điểm gặp",
        fontSize = 16.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(vertical = 8.dp)
    )

    // Nếu không có địa điểm có sẵn, hiển thị thông báo
    if (availableLocations.isEmpty()) {
        Text(
            "Không có địa điểm khả dụng. Vui lòng liên hệ nhà cung cấp.",
            fontSize = 14.sp,
            color = Color.Red,
            modifier = Modifier.padding(vertical = 4.dp)
        )
    }

    Column {
        Box {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { if (availableLocations.isNotEmpty()) expanded = true },
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Text(
                    text = selected ?: "Chọn địa điểm",
                    fontSize = 14.sp,
                    color = if (selected != null) Color.Black else Color.Gray,
                    modifier = Modifier.padding(12.dp)
                )
            }

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .zIndex(1f)
            ) {
                availableLocations.forEach { location ->
                    DropdownMenuItem(
                        text = { Text(location) },
                        onClick = {
                            onSelect(location)
                            expanded = false
                        },
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
            }
        }

        // Thêm nút xem chỉ đường nếu đã chọn địa điểm
        if (selected != null) {
            val context = LocalContext.current
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedButton(
                onClick = {
                    // Mở Google Maps với địa chỉ đã chọn
                    try {
                        val uri = Uri.parse("geo:0,0?q=" + Uri.encode(selected ?: ""))
                        val mapIntent = Intent(Intent.ACTION_VIEW, uri)
                        mapIntent.setPackage("com.google.android.apps.maps")

                        if (mapIntent.resolveActivity(context.packageManager) != null) {
                            context.startActivity(mapIntent)
                        } else {
                            // Fallback nếu không có ứng dụng Maps
                            val browserUri = Uri.parse("https://www.google.com/maps/search/?api=1&query=" + Uri.encode(selected ?: ""))
                            val browserIntent = Intent(Intent.ACTION_VIEW, browserUri)
                            context.startActivity(browserIntent)
                        }
                    } catch (e: Exception) {
                        Toast.makeText(context, "Không thể mở bản đồ: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(36.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = BluePrimary
                ),
                border = BorderStroke(1.dp, BluePrimary)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_directions),
                    contentDescription = "Directions",
                    tint = BluePrimary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("Xem địa điểm trên bản đồ", fontSize = 12.sp)
            }
        }
    }
}

@Composable
fun DateTimePickerDialog(
    onDismiss: () -> Unit,
    onConfirm: (Date, Date) -> Unit,
    duration: Int
) {
    val context = LocalContext.current
    val calendar = Calendar.getInstance()
    var selectedDateTime by remember { mutableStateOf<Date?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Chọn thời gian") },
        text = {
            Column {
                Text("Chọn thời gian bắt đầu")
                Button(onClick = {
                    DatePickerDialog(
                        context,
                        { _, year, month, dayOfMonth ->
                            calendar.set(year, month, dayOfMonth)
                            TimePickerDialog(
                                context,
                                { _, hour, minute ->
                                    calendar.set(Calendar.HOUR_OF_DAY, hour)
                                    calendar.set(Calendar.MINUTE, minute)
                                    calendar.set(Calendar.SECOND, 0)
                                    calendar.set(Calendar.MILLISECOND, 0)
                                    val selected = calendar.time
                                    if (selected.after(Date())) {
                                        selectedDateTime = selected
                                        errorMessage = null
                                    } else {
                                        errorMessage = "Vui lòng chọn thời gian trong tương lai"
                                    }
                                },
                                calendar.get(Calendar.HOUR_OF_DAY),
                                calendar.get(Calendar.MINUTE),
                                true
                            ).show()
                        },
                        calendar.get(Calendar.YEAR),
                        calendar.get(Calendar.MONTH),
                        calendar.get(Calendar.DAY_OF_MONTH)
                    ).apply {
                        datePicker.minDate = System.currentTimeMillis()
                    }.show()
                }) {
                    Text(
                        selectedDateTime?.let { dateFormat.format(it) } ?: "Chọn ngày và giờ",
                        color = if (selectedDateTime != null) Color.Black else Color.Gray
                    )
                }
                errorMessage?.let {
                    Text(
                        text = it,
                        color = Color.Red,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    selectedDateTime?.let { start ->
                        val endCalendar = Calendar.getInstance().apply {
                            time = start
                            add(Calendar.MINUTE, duration)
                        }
                        val end = endCalendar.time
                        onConfirm(start, end)
                    } ?: run {
                        errorMessage = "Vui lòng chọn thời gian trước"
                    }
                },
                enabled = selectedDateTime != null
            ) {
                Text("Xác nhận")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Hủy")
            }
        }
    )
}