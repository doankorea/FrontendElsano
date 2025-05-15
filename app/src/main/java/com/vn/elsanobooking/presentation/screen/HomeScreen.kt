package com.vn.elsanobooking.presentation.screen

import android.location.Location
import android.preference.PreferenceManager
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.vn.elsanobooking.R
import com.vn.elsanobooking.config.Constants
import com.vn.elsanobooking.data.models.Artist
import com.vn.elsanobooking.data.models.Service
import com.vn.elsanobooking.data.models.ServiceApiModel
import com.vn.elsanobooking.presentation.components.LocationPermissionHandler
import com.vn.elsanobooking.ui.theme.*
import com.vn.elsanobooking.viewModel.MakeupArtistViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    onArtistClick: (Int) -> Unit = {}
) {
    val viewModel: MakeupArtistViewModel = viewModel()
    val context = LocalContext.current
    val preferences = PreferenceManager.getDefaultSharedPreferences(context)
    val userName = preferences.getString("userName", "Khách")
    
    // State management
    val artists = viewModel.artists
    val filteredArtists = viewModel.filteredArtists
    val isLoading = viewModel.isLoading
    val allServices = viewModel.allServices
    val selectedServiceId = viewModel.selectedServiceId
    
    var locationText by remember { mutableStateOf("Đang tìm vị trí của bạn...") }
    var hasSetLocation by remember { mutableStateOf(false) }
    var locationRequestKey by remember { mutableStateOf(0) }
    
    val coroutineScope = rememberCoroutineScope()

    // Initial data fetch
    LaunchedEffect(Unit) {
        viewModel.fetchAllServices()
    }

    // Main content
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(top = 42.dp, start = 16.dp, end = 16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp),
        contentPadding = PaddingValues(bottom = 16.dp)
    ) {
        // Welcome header
        item { WelcomeHeader(userName = userName ?: "Khách") }
        
        // Location card
        item { 
            LocationCard(
                locationText = locationText,
                hasSetLocation = hasSetLocation,
                onRefreshLocation = {
                    locationRequestKey++
                    hasSetLocation = false
                    locationText = "Đang tìm vị trí của bạn..."
                }
            )
        }
        
        item { Spacer(modifier = Modifier.height(16.dp)) }
        
        // Services section
        item {
            ServicesSection(
                services = allServices,
                selectedServiceId = selectedServiceId,
                onServiceClick = { service ->
                    val newServiceId = if (selectedServiceId == service.id) null else service.id
                    viewModel.filterArtistsByService(newServiceId)
                }
            )
        }
        
        // Artists section
        item { 
            if (hasSetLocation) {
                Text(
                    text = "Thợ trang điểm gần đây",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }
        }
        
        // Artists list or loading/empty states
        if (hasSetLocation) {
            when {
                isLoading -> {
                    item { LoadingIndicator() }
                }
                filteredArtists.isEmpty() -> {
                    item { EmptyArtistsMessage(selectedServiceId) }
                }
                else -> {
                    items(filteredArtists) { artist ->
                        ArtistCard(
                            artist = artist,
                            onClick = { onArtistClick(artist.id) }
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
        }
    }

    // Location permission handler
    LocationPermissionHandler(
        key = locationRequestKey,
        onLocationResult = { isGranted, location ->
            when {
                isGranted && location != null -> {
                    locationText = "Vị trí hiện tại của bạn"
                    coroutineScope.launch {
                        viewModel.searchArtists(location.latitude, location.longitude)
                        hasSetLocation = true
                    }
                }
                isGranted -> locationText = "Không thể xác định vị trí chính xác"
                else -> locationText = "Không có quyền truy cập vị trí"
            }
        }
    )
}

@Composable
private fun WelcomeHeader(userName: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "Xin chào,",
                fontWeight = FontWeight.W400,
                color = PurpleGrey,
                fontSize = 24.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = userName,
                fontWeight = FontWeight.Bold,
                fontSize = 28.sp
            )
        }
    }
}

@Composable
private fun LocationCard(
    locationText: String,
    hasSetLocation: Boolean,
    onRefreshLocation: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onRefreshLocation() },
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFF5F5F5)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.LocationOn,
                contentDescription = "Location",
                tint = BluePrimary
            )
            
            Spacer(modifier = Modifier.width(8.dp))
            
            Text(
                text = locationText,
                modifier = Modifier.weight(1f),
                color = if (hasSetLocation) Color.Black else Color.Gray
            )
            
            IconButton(onClick = onRefreshLocation) {
                Icon(
                    imageVector = Icons.Default.MyLocation,
                    contentDescription = "Get Current Location"
                )
            }
        }
    }
}

@Composable
private fun ServicesSection(
    services: List<ServiceApiModel>,
    selectedServiceId: Int?,
    onServiceClick: (ServiceApiModel) -> Unit
) {
    Column {
        Text(
            text = "Dịch vụ",
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(services) { service ->
                ServiceItem(
                    service = service,
                    isSelected = selectedServiceId == service.id,
                    onClick = { onServiceClick(service) }
                )
            }
        }
    }
}

@Composable
private fun ServiceItem(
    service: ServiceApiModel,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .padding(vertical = 4.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 4.dp else 1.dp
        ),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) BluePrimary.copy(alpha = 0.1f) else Color.White
        ),
        border = if (isSelected) 
            androidx.compose.foundation.BorderStroke(2.dp, BluePrimary) 
        else 
            null
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = "${Constants.BASE_URL}${service.imageUrl}",
                contentDescription = service.name,
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(if (isSelected) BluePrimary.copy(alpha = 0.2f) else Color(0xFFF5F5F5)),
            )
            
            Spacer(modifier = Modifier.width(8.dp))
            
            Text(
                text = service.name,
                fontSize = 14.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = if (isSelected) BluePrimary else Color.Black
            )
        }
    }
}

@Composable
private fun LoadingIndicator() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun EmptyArtistsMessage(selectedServiceId: Int?) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = if (selectedServiceId != null) 
                "Không tìm thấy thợ trang điểm cho dịch vụ đã chọn" 
            else 
                "Không tìm thấy thợ trang điểm gần đây",
            color = Color.Gray
        )
    }
}

@Composable
private fun ArtistCard(
    artist: Artist,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            ArtistHeader(artist)
            
            Spacer(modifier = Modifier.height(16.dp))
            
            ArtistServices(artist)
        }
    }
}

@Composable
private fun ArtistHeader(artist: Artist) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = artist.avatar.takeIf { it.isNotBlank() }
                ?.let { "${Constants.BASE_URL}$it" } ?: "",
            contentDescription = "${artist.fullName}'s avatar",
            modifier = Modifier
                .size(60.dp)
                .clip(CircleShape)
                .background(Color.Gray),
            placeholder = painterResource(id = android.R.drawable.ic_menu_gallery),
            error = painterResource(id = android.R.drawable.ic_menu_gallery)
        )
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Column {
            Text(
                text = artist.fullName,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = "Location",
                    modifier = Modifier.size(16.dp),
                    tint = Color.Gray
                )
                
                Spacer(modifier = Modifier.width(4.dp))
                
                Text(
                    text = artist.address ?: "Không có địa chỉ",
                    color = Color.Gray,
                    fontSize = 14.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.nail),
                    contentDescription = "Rating",
                    modifier = Modifier.size(16.dp),
                    tint = Color(0xFFFFC107)
                )
                
                Spacer(modifier = Modifier.width(4.dp))
                
                Text(
                    text = "${artist.rating} (${artist.reviewsCount} đánh giá)",
                    color = Color.Gray,
                    fontSize = 14.sp
                )
            }
        }
    }
}

@Composable
private fun ArtistServices(artist: Artist) {
    Text(
        text = "Dịch vụ",
        fontWeight = FontWeight.Bold,
        fontSize = 16.sp,
        modifier = Modifier.padding(bottom = 8.dp)
    )
    
    // Thêm logging để debug
    println("ArtistServices for ${artist.fullName}: services=${artist.services?.size ?: "null"}")
    
    when {
        artist.services == null -> {
            Text(
                text = "Đang tải dịch vụ...",
                color = Color.Gray,
                fontSize = 14.sp
            )
        }
        artist.services?.isEmpty() == true -> {
            Text(
                text = "Không có dịch vụ",
                color = Color.Gray,
                fontSize = 14.sp
            )
        }
        else -> {
            Column {
                artist.services?.take(3)?.forEachIndexed { index, service ->
                    println("Rendering service: ${service.serviceName}")
                    ServiceRow(service = service)
                    if (index < minOf(2, (artist.services?.size ?: 0) - 1)) {
                        Divider(modifier = Modifier.padding(vertical = 8.dp))
                    }
                }
                
                if ((artist.services?.size ?: 0) > 3) {
                    TextButton(
                        onClick = { /* Handle view more */ },
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text("Xem thêm")
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowRight,
                            contentDescription = "More"
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ServiceRow(service: Service) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = service.serviceName,
                fontWeight = FontWeight.Medium,
                fontSize = 16.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            
            Text(
                text = "${service.duration} phút",
                color = Color.Gray,
                fontSize = 14.sp
            )
        }
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Text(
            text = "${service.price.toInt()}đ",
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            color = BluePrimary
        )
    }
}