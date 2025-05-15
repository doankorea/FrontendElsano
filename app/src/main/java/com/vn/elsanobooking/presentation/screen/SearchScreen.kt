package com.vn.elsanobooking.presentation.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.vn.elsanobooking.data.api.RetrofitInstance
import com.vn.elsanobooking.presentation.components.NearbyMakeupArtistCard
import com.vn.elsanobooking.viewModel.MakeupArtistViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(viewModel: MakeupArtistViewModel = viewModel(), navController: NavController, userId: Int) {
    var searchText by remember { mutableStateOf(TextFieldValue("")) }
    var location by remember { mutableStateOf("Hà Nội Hn") }
    var suggestions by remember { mutableStateOf<List<String>>(emptyList()) }
    val coroutineScope = rememberCoroutineScope()
    val artists = viewModel.artists
    val isLoading = viewModel.isLoading
    val uiState = viewModel.uiState

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "ElSANO",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                },
                actions = {
                    IconButton(onClick = { /* Handle close action */ }) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                },
                modifier = Modifier.background(Color.White)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color(0xFFF5F5F5))
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Search Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White, RoundedCornerShape(8.dp))
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Search, contentDescription = "Search Icon", tint = Color.Gray)
                Spacer(modifier = Modifier.width(8.dp))
                BasicTextField(
                    value = searchText,
                    onValueChange = {
                        searchText = it
                        if (it.text.isNotEmpty()) {
                            coroutineScope.launch {
                                searchAddress(it.text) { result ->
                                    suggestions = result
                                }
                            }
                        } else {
                            suggestions = emptyList()
                        }
                    },
                    modifier = Modifier.weight(1f),
                    textStyle = TextStyle(fontSize = 16.sp, color = Color.Black),
                    decorationBox = { innerTextField ->
                        if (searchText.text.isEmpty()) {
                            Text(
                                text = "Nhập địa chỉ để tìm thợ trang điểm",
                                color = Color.Gray,
                                fontSize = 16.sp
                            )
                        }
                        innerTextField()
                    }
                )
            }

            // Suggestions Dropdown
            if (suggestions.isNotEmpty()) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White)
                        .padding(8.dp)
                        .heightIn(max = 200.dp)
                ) {
                    items(suggestions) { suggestion ->
                        Text(
                            text = suggestion,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp)
                                .clickable {
                                    searchText = TextFieldValue(suggestion)
                                    suggestions = emptyList()
                                    coroutineScope.launch {
                                        geocodeAndFetchArtists(
                                            suggestion,
                                            updateLocation = { location = it },
                                            setUiState = { viewModel.uiState = it },
                                            searchArtists = { lat, lng, radius ->
                                                viewModel.searchArtists(lat, lng, radius)
                                            }
                                        )
                                    }
                                },
                            fontSize = 14.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Location Tag
            Row(
                modifier = Modifier
                    .background(Color.White, RoundedCornerShape(16.dp))
                    .padding(horizontal = 12.dp, vertical = 8.dp)
                    .clickable { /* Handle location click */ },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.LocationOn, contentDescription = "Location Icon", tint = Color.Gray)
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = location, fontSize = 14.sp, color = Color.Black)
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    Icons.Default.Close,
                    contentDescription = "Remove Location",
                    tint = Color.Gray,
                    modifier = Modifier.size(16.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Nearby Makeup Artists Section
            Text(
                text = "Nearby Makeup Artists",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
            Spacer(modifier = Modifier.height(8.dp))

            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            } else {
                when (uiState) {
                    is MakeupArtistViewModel.UiState.Error -> {
                        Text(
                            text = (uiState as MakeupArtistViewModel.UiState.Error).message,
                            color = Color.Red,
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        )
                    }
                    is MakeupArtistViewModel.UiState.Idle -> {
                        Text(
                            text = "Vui lòng nhập địa chỉ để tìm thợ trang điểm",
                            color = Color.Gray,
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        )
                    }
                    is MakeupArtistViewModel.UiState.Success -> {
                        if (artists.isEmpty()) {
                            Text(
                                text = "Không tìm thấy thợ trang điểm gần đây",
                                color = Color.Gray,
                                modifier = Modifier.align(Alignment.CenterHorizontally)
                            )
                        } else {
                            LazyColumn(modifier = Modifier.fillMaxHeight()) {
                                items(artists) { artist ->
                                    NearbyMakeupArtistCard(
                                        artist = artist,
                                        navController = navController,
                                        userId = userId,
                                        modifier = Modifier.padding(vertical = 8.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// --------------------------
// External helper functions
// --------------------------

suspend fun searchAddress(query: String, updateSuggestions: (List<String>) -> Unit) {
    try {
        val response = RetrofitInstance.authApi.searchAddress(query)
        updateSuggestions(response.predictions.map { it.description })
        println("Address suggestions: ${response.predictions.map { it.description }}")
    } catch (e: Exception) {
        updateSuggestions(emptyList())
        println("Address search error: ${e.message}")
    }
}

suspend fun geocodeAndFetchArtists(
    address: String,
    updateLocation: (String) -> Unit,
    setUiState: (MakeupArtistViewModel.UiState) -> Unit,
    searchArtists: (Double, Double, Double) -> Unit
) {
    try {
        val response = RetrofitInstance.authApi.searchAddress(address)
        val placeId = response.predictions.firstOrNull()?.place_id
        if (placeId == null) {
            setUiState(MakeupArtistViewModel.UiState.Error("Không tìm thấy placeId"))
            return
        }
        val coordinates = RetrofitInstance.authApi.getCoordinates(placeId)
        println("Coordinates: ${coordinates.latitude}, ${coordinates.longitude}")
        updateLocation(address)
        searchArtists(coordinates.latitude, coordinates.longitude, 10.0)
    } catch (e: Exception) {
        setUiState(MakeupArtistViewModel.UiState.Error("Không tìm thấy địa chỉ: ${e.message}"))
    }
}
