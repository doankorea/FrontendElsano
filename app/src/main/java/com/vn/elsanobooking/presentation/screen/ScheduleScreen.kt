@file:OptIn(ExperimentalMaterial3Api::class)

package com.vn.elsanobooking.presentation.screen

import android.content.Intent
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.preference.PreferenceManager
import com.vn.elsanobooking.R
import com.vn.elsanobooking.config.Constants
import com.vn.elsanobooking.data.api.RetrofitInstance
import com.vn.elsanobooking.data.api.VnpayDirectClient
import com.vn.elsanobooking.data.models.AppointmentResponse
import com.vn.elsanobooking.data.models.ReviewResponse
import com.vn.elsanobooking.data.models.VnpayPaymentRequest
import com.vn.elsanobooking.ui.theme.*
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

enum class AppointmentFilter {
    UPCOMING, COMPLETED, CANCELLED, ALL
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleScreen(
    modifier: Modifier = Modifier,
    onNavigateToChat: (Int) -> Unit = {}
) {
    val context = LocalContext.current
    val preferences = PreferenceManager.getDefaultSharedPreferences(context)
    val userId = preferences.getInt("userId", -1)

    val coroutineScope = rememberCoroutineScope()
    var appointments by remember { mutableStateOf<List<AppointmentResponse>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var selectedFilter by remember { mutableStateOf(AppointmentFilter.UPCOMING) }
    var selectedAppointment by remember { mutableStateOf<AppointmentResponse?>(null) }
    var showAppointmentDetail by remember { mutableStateOf(false) }
    var refreshTrigger by remember { mutableStateOf(0) }

    // Biến cho kết quả thanh toán
    var showPaymentResultDialog by remember { mutableStateOf(false) }
    var paymentResultSuccess by remember { mutableStateOf(false) }
    var paymentResultMessage by remember { mutableStateOf("") }

    // Function to refresh appointments data
    fun refreshAppointments() {
        coroutineScope.launch {
            if (userId != -1) {
                isLoading = true
                try {
                    Log.d("ScheduleScreen", "Refreshing appointments for user $userId")
                    val response = RetrofitInstance.bookingApi.getUserAppointments(userId)
                    if (response.success) {
                        appointments = response.data ?: emptyList()
                        Log.d("ScheduleScreen", "Received ${appointments.size} appointments")
                        error = null
                    } else {
                        error = "Không thể tải lịch hẹn: ${response.message ?: "Lỗi không xác định"}"
                        Log.e("ScheduleScreen", "API error: ${response.message}")
                    }
                } catch (e: Exception) {
                    Log.e("ScheduleScreen", "Error fetching appointments: ${e.message}", e)
                    error = "Không thể tải lịch hẹn: ${e.message}"
                } finally {
                    isLoading = false
                }
            }
        }
    }

    // Fetch appointments when the screen loads or after refresh is triggered
    LaunchedEffect(userId, refreshTrigger) {
        refreshAppointments()
    }

    // Chuẩn bị dữ liệu lịch hẹn đã được sắp xếp
    val appointmentsByDate = remember(appointments) {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())

        // Sắp xếp lịch hẹn theo thời gian (mặc định)
        val sortedByTime = appointments.sortedBy { appointment ->
            try {
                dateFormat.parse(appointment.appointmentDate)?.time ?: Long.MAX_VALUE
            } catch (e: Exception) {
                Log.e("ScheduleScreen", "Error parsing date: ${e.message}", e)
                Long.MAX_VALUE
            }
        }

        // Sắp xếp lịch hẹn theo thứ tự giảm dần (mới nhất trước)
        val sortedByTimeDesc = appointments.sortedByDescending { appointment ->
            try {
                dateFormat.parse(appointment.appointmentDate)?.time ?: 0
            } catch (e: Exception) {
                Log.e("ScheduleScreen", "Error parsing date: ${e.message}", e)
                0
            }
        }

        // Lưu cả hai kiểu sắp xếp
        mapOf(
            "asc" to sortedByTime,      // Tăng dần (dùng cho lịch sắp tới)
            "desc" to sortedByTimeDesc  // Giảm dần (dùng cho lịch đã qua)
        )
    }

    // Lọc và sắp xếp theo loại đã chọn
    val filteredAppointments = when (selectedFilter) {
        AppointmentFilter.UPCOMING -> appointmentsByDate["asc"]!!.filter {
            it.status == "Pending" || it.status == "Confirmed"
        }
        AppointmentFilter.COMPLETED -> appointmentsByDate["desc"]!!.filter {
            it.status == "Completed"
        }
        AppointmentFilter.CANCELLED -> appointmentsByDate["desc"]!!.filter {
            it.status == "Cancelled"
        }
        AppointmentFilter.ALL -> appointmentsByDate["desc"]!! // Mặc định hiển thị mới nhất trước
    }

    if (showAppointmentDetail && selectedAppointment != null) {
        AppointmentDetailSheet(
            appointment = selectedAppointment!!,
            onDismiss = {
                showAppointmentDetail = false
                // Refresh data after dialog is dismissed to show updated status
                refreshTrigger += 1
            },
            onNavigateToChat = onNavigateToChat,
            onPaymentComplete = { success, message ->
                showPaymentResultDialog = true
                paymentResultSuccess = success
                paymentResultMessage = message
                refreshTrigger += 1  // Refresh data after payment
            }
        )
    }

    // Dialog hiển thị kết quả thanh toán ở mức ScheduleScreen
    if (showPaymentResultDialog) {
        AlertDialog(
            onDismissRequest = {
                showPaymentResultDialog = false
                if (paymentResultSuccess) {
                    showAppointmentDetail = false // Đóng sheet chi tiết nếu thanh toán thành công
                }
            },
            title = {
                Text(
                    text = if (paymentResultSuccess) "Thanh toán thành công" else "Thông báo",
                    fontWeight = FontWeight.Bold,
                    fontFamily = poppinsFontFamily
                )
            },
            text = {
                Column {
                    Text(
                        text = paymentResultMessage,
                        fontFamily = poppinsFontFamily
                    )
                    if (paymentResultSuccess) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Lịch hẹn của bạn đã được xác nhận.",
                            fontWeight = FontWeight.Medium,
                            color = GreenStatus,
                            fontFamily = poppinsFontFamily
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showPaymentResultDialog = false
                        if (paymentResultSuccess) {
                            showAppointmentDetail = false // Đóng sheet chi tiết nếu thanh toán thành công
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (paymentResultSuccess) GreenStatus else BluePrimary
                    )
                ) {
                    Text("OK", fontFamily = poppinsFontFamily)
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Lịch hẹn của tôi",
                        fontFamily = poppinsFontFamily,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Filter tabs
            LazyRow(
                modifier = Modifier.padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(AppointmentFilter.values()) { filter ->
                    CategorySchedule(
                        title = when (filter) {
                            AppointmentFilter.UPCOMING -> "Sắp tới"
                            AppointmentFilter.COMPLETED -> "Đã hoàn thành"
                            AppointmentFilter.CANCELLED -> "Đã hủy"
                            AppointmentFilter.ALL -> "Tất cả"
                        },
                        isSelected = filter == selectedFilter,
                        onClick = { selectedFilter = filter }
                    )
                }
            }

            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (error != null) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(error!!, color = Color.Red)
                }
            } else if (filteredAppointments.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Không có lịch hẹn nào", color = Color.Gray)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(filteredAppointments) { appointment ->
                        AppointmentCard(
                            appointment = appointment,
                            onClick = {
                                selectedAppointment = appointment
                                showAppointmentDetail = true
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CategorySchedule(
    title: String,
    isSelected: Boolean = false,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .wrapContentWidth()
            .padding(top = 20.dp)
            .clickable(onClick = onClick),
        color = if (isSelected) Color(0xFF63B4FF).copy(alpha = 0.1f) else Color.Transparent,
        shape = RoundedCornerShape(100.dp)
    ) {
        Text(
            modifier = Modifier.padding(vertical = 16.dp, horizontal = 16.dp),
            text = title,
            fontFamily = poppinsFontFamily,
            color = if (isSelected) BluePrimary else Color.Gray,
            fontWeight = FontWeight.Medium,
            fontSize = 16.sp
        )
    }
}

@Composable
fun AppointmentCard(
    appointment: AppointmentResponse,
    onClick: () -> Unit
) {
    val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
    val displayFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    val currencyFormat = NumberFormat.getCurrencyInstance(Locale("vi", "VN"))

    val appointmentDate = try {
        displayFormat.format(dateFormat.parse(appointment.appointmentDate)!!)
    } catch (e: Exception) {
        Log.e("ScheduleScreen", "Error parsing date: ${e.message}", e)
        appointment.appointmentDate
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = appointment.artistName,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    fontFamily = poppinsFontFamily
                )
                StatusChip(status = appointment.status)
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = appointment.service.serviceName,
                fontWeight = FontWeight.Normal,
                fontSize = 16.sp,
                color = Color.DarkGray,
                fontFamily = poppinsFontFamily
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Thời gian: $appointmentDate (${appointment.service.duration} phút)",
                fontSize = 14.sp,
                color = Color.Gray,
                fontFamily = poppinsFontFamily
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Địa điểm: ${appointment.location.address}",
                fontSize = 14.sp,
                color = Color.Gray,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                fontFamily = poppinsFontFamily
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Giá: ${currencyFormat.format(appointment.service.price)}",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    fontFamily = poppinsFontFamily
                )

                appointment.payment?.let { payment ->
                    Text(
                        text = "Thanh toán: ${
                            when (payment.paymentStatus) {
                                "Paid" -> "Đã thanh toán"
                                "Pending" -> "Chờ thanh toán"
                                "Cancelled" -> "Đã hủy"
                                else -> payment.paymentStatus
                            }
                        }",
                        fontSize = 14.sp,
                        color = when (payment.paymentStatus) {
                            "Paid" -> GreenStatus
                            "Pending" -> OrangeStatus
                            "Cancelled" -> RedStatus
                            else -> Color.Gray
                        },
                        fontFamily = poppinsFontFamily
                    )
                }
            }
        }
    }
}

@Composable
fun StatusChip(status: String) {
    val (backgroundColor, textColor) = when (status) {
        "Pending" -> Pair(OrangeStatus.copy(alpha = 0.2f), OrangeStatus)
        "Confirmed" -> Pair(BluePrimary.copy(alpha = 0.2f), BluePrimary)
        "Completed" -> Pair(GreenStatus.copy(alpha = 0.2f), GreenStatus)
        "Cancelled" -> Pair(RedStatus.copy(alpha = 0.2f), RedStatus)
        else -> Pair(Color.Gray.copy(alpha = 0.2f), Color.Gray)
    }

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = backgroundColor
    ) {
        Text(
            text = when (status) {
                "Pending" -> "Chờ xác nhận"
                "Confirmed" -> "Đã xác nhận"
                "Completed" -> "Hoàn thành"
                "Cancelled" -> "Đã hủy"
                else -> status
            },
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
            color = textColor,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            fontFamily = poppinsFontFamily
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppointmentDetailSheet(
    appointment: AppointmentResponse,
    onDismiss: () -> Unit,
    onNavigateToChat: (Int) -> Unit,
    onPaymentComplete: (Boolean, String) -> Unit
) {
    val context = LocalContext.current
    val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
    val displayFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    val currencyFormat = NumberFormat.getCurrencyInstance(Locale("vi", "VN"))
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    var isLoading by remember { mutableStateOf(false) }
    var showCancelDialog by remember { mutableStateOf(false) }
    var showResultDialog by remember { mutableStateOf(false) }
    var showRatingDialog by remember { mutableStateOf(false) }
    var resultMessage by remember { mutableStateOf("") }
    var resultSuccess by remember { mutableStateOf(false) }
    var paymentUrlToOpen by remember { mutableStateOf<String?>(null) }
    var isPaymentProcessing by remember { mutableStateOf(false) }
    var lastProcessedAppointmentId by remember { mutableStateOf<Int?>(null) }

    // Format dates
    val appointmentDate = try {
        displayFormat.format(dateFormat.parse(appointment.appointmentDate)!!)
    } catch (e: Exception) {
        appointment.appointmentDate
    }

    val endTime = try {
        val date = dateFormat.parse(appointment.appointmentDate)!!
        val calendar = Calendar.getInstance()
        calendar.time = date
        calendar.add(Calendar.MINUTE, appointment.service.duration)
        displayFormat.format(calendar.time)
    } catch (e: Exception) {
        "Không xác định"
    }

    // Determine if appointment can be cancelled
    val canCancel = appointment.status == "Pending" || appointment.status == "Confirmed"

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Chi tiết lịch hẹn",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = poppinsFontFamily
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Trạng thái",
                    fontSize = 14.sp,
                    color = Color.Gray,
                    fontFamily = poppinsFontFamily
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    StatusChip(status = appointment.status)

                    // Hiển thị icon thanh toán nếu chưa thanh toán
                    if (appointment.payment?.paymentStatus == "Pending") {
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            painter = painterResource(id = R.drawable.ic_payment),
                            contentDescription = "Payment Required",
                            tint = OrangeStatus,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            "Chưa thanh toán",
                            color = OrangeStatus,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(start = 4.dp)
                        )
                    }
                }
            }

            Divider(modifier = Modifier.padding(vertical = 12.dp))

            DetailRow(label = "Nghệ sĩ", value = appointment.artistName)
            DetailRow(label = "Dịch vụ", value = appointment.service.serviceName)
            DetailRow(label = "Thời gian bắt đầu", value = appointmentDate)
            DetailRow(label = "Thời gian kết thúc", value = endTime)
            DetailRow(label = "Thời lượng", value = "${appointment.service.duration} phút")
            DetailRow(label = "Địa điểm", value = appointment.location.address)
            DetailRow(label = "Giá dịch vụ", value = currencyFormat.format(appointment.service.price))

            // Thêm nút chỉ đường đến địa điểm hẹn
            Spacer(modifier = Modifier.height(8.dp))

            // Hàng chứa nút chỉ đường và nhắn tin
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Nút chỉ đường
                OutlinedButton(
                    onClick = {
                        try {
                            // Thử sử dụng tọa độ nếu có
                            if (appointment.location.latitude != 0.0 && appointment.location.longitude != 0.0) {
                                val uri = Uri.parse("google.navigation:q=${appointment.location.latitude},${appointment.location.longitude}")
                                val mapIntent = Intent(Intent.ACTION_VIEW, uri)
                                mapIntent.setPackage("com.google.android.apps.maps")

                                // Kiểm tra xem có ứng dụng Maps không
                                if (mapIntent.resolveActivity(context.packageManager) != null) {
                                    context.startActivity(mapIntent)
                                } else {
                                    // Fallback dùng browser
                                    val browserUri = Uri.parse("https://www.google.com/maps/dir/?api=1&destination=${appointment.location.latitude},${appointment.location.longitude}")
                                    val browserIntent = Intent(Intent.ACTION_VIEW, browserUri)
                                    context.startActivity(browserIntent)
                                }
                            } else {
                                // Nếu không có tọa độ, dùng địa chỉ text
                                val uri = Uri.parse("geo:0,0?q=" + Uri.encode(appointment.location.address ?: ""))
                                val mapIntent = Intent(Intent.ACTION_VIEW, uri)
                                mapIntent.setPackage("com.google.android.apps.maps")

                                if (mapIntent.resolveActivity(context.packageManager) != null) {
                                    context.startActivity(mapIntent)
                                } else {
                                    val browserUri = Uri.parse("https://www.google.com/maps/search/?api=1&query=" + Uri.encode(appointment.location.address ?: ""))
                                    val browserIntent = Intent(Intent.ACTION_VIEW, browserUri)
                                    context.startActivity(browserIntent)
                                }
                            }
                        } catch (e: Exception) {
                            Toast.makeText(context, "Không thể mở bản đồ: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.weight(1f),
                    border = BorderStroke(1.dp, BluePrimary),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = BluePrimary)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_directions),
                        contentDescription = "Directions",
                        tint = BluePrimary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Chỉ đường", fontFamily = poppinsFontFamily)
                }

                // Nút nhắn tin với artist
                OutlinedButton(
                    onClick = {
                        // Điều hướng đến trang chat với artist
                        onNavigateToChat(appointment.artistId)
                    },
                    modifier = Modifier.weight(1f),
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
                    Text(
                        "Nhắn tin với ${appointment.artistName.split(" ").lastOrNull() ?: "nhà cung cấp"}",
                        fontFamily = poppinsFontFamily,
                        fontSize = 13.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            appointment.payment?.let { payment ->
                Divider(modifier = Modifier.padding(vertical = 12.dp))

                Text(
                    text = "Thông tin thanh toán",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = poppinsFontFamily
                )

                Spacer(modifier = Modifier.height(8.dp))

                DetailRow(label = "Phương thức", value = payment.paymentMethod)
                DetailRow(
                    label = "Trạng thái",
                    value = when (payment.paymentStatus) {
                        "Paid" -> "Đã thanh toán"
                        "Pending" -> "Chờ thanh toán"
                        "Cancelled" -> "Đã hủy"
                        else -> payment.paymentStatus
                    },
                    valueColor = when (payment.paymentStatus) {
                        "Paid" -> GreenStatus
                        "Pending" -> OrangeStatus
                        "Cancelled" -> RedStatus
                        else -> Color.Black
                    }
                )
                DetailRow(label = "Số tiền", value = currencyFormat.format(payment.amount))
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Thêm nút hủy lịch hẹn nếu status là Pending hoặc Confirmed
            if (appointment.status == "Pending" || appointment.status == "Confirmed") {
                if (appointment.payment?.paymentStatus == "Pending") {
                    // Nếu cần thanh toán, hiển thị cả hai nút cùng hàng
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Nút hủy lịch
                        OutlinedButton(
                            onClick = { showCancelDialog = true },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = RedStatus),
                            border = BorderStroke(1.dp, RedStatus)
                        ) {
                            Text(
                                "Hủy lịch hẹn",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        // Nút thanh toán
                        Button(
                            onClick = {
                                coroutineScope.launch {
                                    try {
                                        isLoading = true
                                        val paymentUrl = getPaymentUrl(
                                            appointmentId = appointment.appointmentId,
                                            amount = appointment.payment?.amount ?: 0.0
                                        )

                                        if (paymentUrl != null) {
                                            paymentUrlToOpen = paymentUrl
                                            isPaymentProcessing = true
                                            lastProcessedAppointmentId = appointment.appointmentId
                                        } else {
                                            onPaymentComplete(false, "Không lấy được link thanh toán. Vui lòng thử lại sau.")
                                        }
                                    } catch (e: Exception) {
                                        Log.e("PaymentDebug", "Lỗi khi xử lý thanh toán: ${e.message}", e)
                                        onPaymentComplete(false, "Lỗi khi xử lý thanh toán: ${e.message}")
                                    } finally {
                                        isLoading = false
                                    }
                                }
                            },
                            modifier = Modifier
                                .height(54.dp)
                                .weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF4CAF50),
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(8.dp),
                            enabled = !isLoading,
                            elevation = ButtonDefaults.buttonElevation(
                                defaultElevation = 4.dp,
                                pressedElevation = 8.dp
                            )
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Đang xử lý...", color = Color.White)
                            } else {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_payment),
                                    contentDescription = "Payment Icon",
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    "Thanh toán",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }
                } else {
                    // Nếu không cần thanh toán, chỉ hiển thị nút hủy
                    OutlinedButton(
                        onClick = { showCancelDialog = true },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = RedStatus),
                        border = BorderStroke(1.dp, RedStatus)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_arrow_back),
                            contentDescription = "Cancel",
                            tint = RedStatus,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Hủy lịch hẹn",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            } else if (appointment.payment?.paymentStatus == "Pending") {
                // Nếu không thể hủy nhưng vẫn cần thanh toán
                Button(
                    onClick = {
                        coroutineScope.launch {
                            try {
                                isLoading = true
                                val paymentUrl = getPaymentUrl(
                                    appointmentId = appointment.appointmentId,
                                    amount = appointment.payment?.amount ?: 0.0
                                )

                                if (paymentUrl != null) {
                                    paymentUrlToOpen = paymentUrl
                                    isPaymentProcessing = true
                                    lastProcessedAppointmentId = appointment.appointmentId
                                } else {
                                    onPaymentComplete(false, "Không lấy được link thanh toán. Vui lòng thử lại sau.")
                                }
                            } catch (e: Exception) {
                                Log.e("PaymentDebug", "Lỗi khi xử lý thanh toán: ${e.message}", e)
                                onPaymentComplete(false, "Lỗi khi xử lý thanh toán: ${e.message}")
                            } finally {
                                isLoading = false
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF4CAF50),
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(8.dp),
                    enabled = !isLoading,
                    elevation = ButtonDefaults.buttonElevation(
                        defaultElevation = 4.dp,
                        pressedElevation = 8.dp
                    )
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Đang xử lý...", color = Color.White)
                    } else {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_payment),
                                contentDescription = "Payment Icon",
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                "Thanh toán ngay",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Thêm nút đánh giá nếu lịch hẹn đã hoàn thành
            if (appointment.status == "Completed") {
                Button(
                    onClick = { showRatingDialog = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF4CAF50),
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_star),
                        contentDescription = "Rating",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Đánh giá nghệ sĩ",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }

    // Rating Dialog
    if (showRatingDialog) {
        RatingDialog(
            artistName = appointment.artistName,
            appointmentId = appointment.appointmentId,
            onDismiss = { showRatingDialog = false },
            onRatingSubmitted = { success, message ->
                showRatingDialog = false
                coroutineScope.launch {
                    snackbarHostState.showSnackbar(
                        message = message,
                        duration = SnackbarDuration.Short
                    )
                }
            }
        )
    }

    // Hiển thị snackbar nếu có
    Box(modifier = Modifier.fillMaxSize()) {
        SnackbarHost(hostState = snackbarHostState, modifier = Modifier.align(Alignment.BottomCenter))
    }
}

@Composable
fun DetailRow(
    label: String,
    value: String,
    valueColor: Color = Color.Black
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            color = Color.Gray,
            fontFamily = poppinsFontFamily
        )
        Text(
            text = value,
            fontSize = 14.sp,
            color = valueColor,
            fontWeight = FontWeight.Medium,
            fontFamily = poppinsFontFamily
        )
    }
}

suspend fun getPaymentUrl(appointmentId: Int, amount: Double): String? {
    Log.d("PaymentDebug", "Getting payment URL for appointment $appointmentId with amount $amount")
    return try {
        val request = VnpayPaymentRequest(
            appointmentId = appointmentId,
            orderType = "other",
            amount = amount
        )
        Log.d("PaymentDebug", "Created payment request: $request")

        // Thử lấy payment URL bằng direct client trước
        var paymentUrl = VnpayDirectClient.getPaymentUrl(request)
        Log.d("PaymentDebug", "Direct client returned URL: $paymentUrl")

        // Nếu không thành công, thử dùng Retrofit API
        if (paymentUrl == null) {
            try {
                Log.d("PaymentDebug", "Trying Retrofit API...")
                val response = RetrofitInstance.vnpayApi.createVnpayUrl(request)
                val responseString = response.string()
                Log.d("PaymentDebug", "Retrofit API raw response: $responseString")

                paymentUrl = responseString

                // Validate URL format
                if (!responseString.startsWith("http")) {
                    Log.e("PaymentDebug", "Invalid URL format returned from Retrofit: $responseString")
                    // Thử trích xuất URL từ phản hồi bất thường
                    if (responseString.contains("http")) {
                        val urlRegex = "(https?://[^\\s\"'<>]+)".toRegex()
                        val match = urlRegex.find(responseString)
                        if (match != null) {
                            val extractedUrl = match.value
                            Log.d("PaymentDebug", "Extracted URL from response: $extractedUrl")
                            paymentUrl = extractedUrl
                        } else {
                            paymentUrl = null
                        }
                    } else {
                        paymentUrl = null
                    }
                }
            } catch (e: Exception) {
                Log.e("PaymentDebug", "Retrofit API error: ${e.message}", e)
                paymentUrl = null
            }
        }

        Log.d("PaymentDebug", "Final payment URL: $paymentUrl")
        paymentUrl
    } catch (e: Exception) {
        Log.e("PaymentDebug", "Error getting payment URL: ${e.message}", e)
        e.printStackTrace()
        null
    }
}

@Composable
fun RatingDialog(
    artistName: String,
    appointmentId: Int,
    onDismiss: () -> Unit,
    onRatingSubmitted: (Boolean, String) -> Unit
) {
    var rating by remember { mutableStateOf(0f) }
    var comment by remember { mutableStateOf("") }
    var isSubmitting by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    "Đánh giá nghệ sĩ",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    fontFamily = poppinsFontFamily
                )
                Text(
                    artistName,
                    fontSize = 16.sp,
                    color = Color.Gray,
                    fontFamily = poppinsFontFamily
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Rating Stars
                Row(
                    modifier = Modifier.padding(vertical = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    repeat(5) { index ->
                        Icon(
                            painter = painterResource(
                                id = if (index < rating) R.drawable.ic_star_filled
                                else R.drawable.ic_star
                            ),
                            contentDescription = "Star ${index + 1}",
                            tint = if (index < rating) Color(0xFFFFC107) else Color.Gray,
                            modifier = Modifier
                                .size(36.dp)
                                .clickable { rating = index + 1f }
                        )
                    }
                }

                // Comment TextField
                OutlinedTextField(
                    value = comment,
                    onValueChange = { comment = it },
                    label = { Text("Nhận xét của bạn") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = Color.Gray
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (rating == 0f) {
                        onRatingSubmitted(false, "Vui lòng chọn số sao đánh giá")
                        return@Button
                    }

                    isSubmitting = true
                    coroutineScope.launch {
                        try {
                            val response = RetrofitInstance.ratingApi.submitRating(
                                appointmentId = appointmentId,
                                rating = rating.toInt(),
                                comment = comment
                            )
                            // Display the message from ReviewResponse on success
                            onRatingSubmitted(true, response.message)
                        } catch (e: Exception) {
                            onRatingSubmitted(false, "Lỗi: ${e.message}")
                        } finally {
                            isSubmitting = false
                        }
                    }
                },
                enabled = !isSubmitting,
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Gửi đánh giá")
                }
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Hủy")
            }
        }
    )
}