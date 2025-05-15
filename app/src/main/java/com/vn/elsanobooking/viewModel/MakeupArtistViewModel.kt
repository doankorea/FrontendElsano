package com.vn.elsanobooking.viewModel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vn.elsanobooking.data.api.RetrofitInstance
import com.vn.elsanobooking.data.models.Artist
import com.vn.elsanobooking.data.models.Service
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

class MakeupArtistViewModel : ViewModel() {
    var artists by mutableStateOf<List<Artist>>(emptyList())
    var filteredArtists by mutableStateOf<List<Artist>>(emptyList())
    var selectedArtist by mutableStateOf<Artist?>(null)
    var services by mutableStateOf<List<Service>>(emptyList())
    var allServices by mutableStateOf<List<com.vn.elsanobooking.data.models.ServiceApiModel>>(emptyList())
    var uiState by mutableStateOf<UiState>(UiState.Idle)
    var isLoading by mutableStateOf(false)
    var selectedServiceId by mutableStateOf<Int?>(null)

    private val api = RetrofitInstance.makeupArtistApi
    private val generalApi = RetrofitInstance.generalApi

    fun searchArtists(latitude: Double, longitude: Double, radius: Double = 10.0) {
        viewModelScope.launch {
            isLoading = true
            uiState = UiState.Idle
            println("Searching artists: lat=$latitude, lon=$longitude")
            try {
                val response = api.searchArtists(latitude, longitude, radius)
                println("API Response received. Artists count: ${response.size}")
                
                // Fetch detailed information for each artist
                val detailedArtists = response.map { artist ->
                    try {
                        val artistDetails = api.getArtistById(artist.id)
                        println("Fetched details for artist ${artist.id}. Services found: ${artistDetails.services?.size ?: 0}")
                        artistDetails
                    } catch (e: Exception) {
                        println("Error fetching details for artist ${artist.id}: ${e.message}")
                        artist // Return the original artist if details cannot be fetched
                    }
                }
                
                artists = detailedArtists
                
                // When we get new artists, reset filtered artists
                updateFilteredArtists()
                
                println("Artists update completed:")
                artists.forEach { artist ->
                    println("Artist ${artist.id} - ${artist.fullName}:")
                    println("  Services count: ${artist.services?.size ?: 0}")
                    artist.services?.forEach { service ->
                        println("  - Service: ${service.serviceId} - ${service.serviceName} - ${service.price}đ")
                    }
                }
                
                uiState = if (artists.isNotEmpty()) {
                    UiState.Success("Tìm kiếm thành công")
                } else {
                    UiState.Error("Không tìm thấy thợ trang điểm gần đây")
                }
            } catch (e: HttpException) {
                println("HTTP Error: ${e.message()}")
                println("Error response: ${e.response()?.errorBody()?.string()}")
                uiState = UiState.Error("Lỗi HTTP: ${e.message()}")
            } catch (e: IOException) {
                println("Network Error: ${e.message}")
                println("Stack trace: ${e.stackTraceToString()}")
                uiState = UiState.Error("Lỗi mạng: ${e.message}")
            } catch (e: Exception) {
                println("Unexpected error: ${e.message}")
                println("Stack trace: ${e.stackTraceToString()}")
                uiState = UiState.Error("Lỗi không xác định: ${e.message}")
            } finally {
                isLoading = false
                println("Search completed: isLoading=$isLoading, uiState=$uiState")
            }
        }
    }

    fun filterArtistsByService(serviceId: Int?) {
        println("Filtering artists by service: $serviceId")
        selectedServiceId = serviceId
        
        // When changing service filter, update filtered artists
        updateFilteredArtists()
        
        println("Filtered artists count: ${filteredArtists.size}")
        
        // Additional debug
        if (selectedServiceId != null) {
            filteredArtists.forEach { artist ->
                println("Filtered artist: ${artist.fullName}")
                val matchingServices = artist.services?.filter { service ->
                    service.serviceId == selectedServiceId
                } ?: emptyList()
                
                println("  Matching services: ${matchingServices.size}")
                matchingServices.forEach { service ->
                    println("    Service: ${service.serviceName}")
                }
            }
        }
    }

    private fun updateFilteredArtists() {
        filteredArtists = if (selectedServiceId != null) {
            println("Filtering artists by serviceId: $selectedServiceId")
            
            // Get artists who offer this service
            val matchingArtists = artists.filter { artist ->
                // Check if any service matches the selected service type ID
                // Try multiple fields to see which one works
                val hasMatchingService = artist.services?.any { service ->
                    // Try serviceId field
                    val matchesServiceId = service.serviceId == selectedServiceId
                    
                    // Try serviceDetailId field as fallback
                    val matchesDetailId = service.serviceDetailId == selectedServiceId
                    
                    // Debug info
                    if (matchesServiceId || matchesDetailId) {
                        println("MATCH: Artist=${artist.fullName}, Service=${service.serviceName}, " +
                                "serviceId=${service.serviceId}, serviceDetailId=${service.serviceDetailId}")
                    }
                    
                    // Match if either field matches
                    matchesServiceId || matchesDetailId
                } ?: false
                
                hasMatchingService
            }
            
            println("Found ${matchingArtists.size} matching artists out of ${artists.size} total")
            matchingArtists
        } else {
            println("No service selected, showing all ${artists.size} artists")
            artists
        }
    }

    fun getArtistDetails(artistId: Int) {
        viewModelScope.launch {
            isLoading = true
            uiState = UiState.Idle
            try {
                selectedArtist = api.getArtistById(artistId)
                println("Artist details: $selectedArtist")

                // Debug thông tin về appointments
                println("Artist appointments: ${selectedArtist?.appointments?.size ?: 0}")
                selectedArtist?.appointments?.forEach { appointment ->
                    println("Appointment: id=${appointment.appointmentId}, serviceDetailId=${appointment.serviceDetailId}")
                }

                // Lấy danh sách dịch vụ từ artist
                services = selectedArtist?.services ?: emptyList()
                println("Services: $services")

                // In chi tiết về mỗi dịch vụ để debug
                services.forEachIndexed { index, service ->
                    println("Service[$index]: ID=${service.serviceId}, Name=${service.serviceName}, Price=${service.price}, Duration=${service.duration}, ServiceDetailId=${service.serviceDetailId}")
                }

                // Luôn đảm bảo rằng mỗi service đều có serviceDetailId
                if (services.any { it.serviceDetailId == null }) {
                    println("Có một số service không có serviceDetailId, đang cập nhật...")

                    // Tạo một bản sao của danh sách dịch vụ để có thể cập nhật
                    val updatedServices = services.map { service ->
                        if (service.serviceDetailId != null) {
                            // Nếu đã có serviceDetailId, giữ nguyên
                            service
                        } else {
                            // Tìm appointment có liên quan đến service này
                            val matchingAppointment = selectedArtist?.appointments?.find { appointment ->
                                // Giả sử rằng serviceDetailId trong appointment liên quan đến serviceId
                                // Đây chỉ là một giả định, có thể cần thay đổi tùy thuộc vào thiết kế thực tế
                                appointment.serviceDetailId == service.serviceId
                            }

                            if (matchingAppointment != null) {
                                // Nếu tìm thấy appointment, sử dụng serviceDetailId của nó
                                println("Tìm thấy appointment cho service=${service.serviceId}, sử dụng serviceDetailId=${matchingAppointment.serviceDetailId}")
                                service.copy(serviceDetailId = matchingAppointment.serviceDetailId)
                            } else {
                                // Nếu không tìm thấy appointment, sử dụng serviceId làm serviceDetailId
                                // Đây là một giả định, có thể cần thay đổi tùy thuộc vào thiết kế thực tế
                                println("Không tìm thấy appointment cho service=${service.serviceId}, sử dụng serviceId làm serviceDetailId")
                                service.copy(serviceDetailId = service.serviceId)
                            }
                        }
                    }

                    // Cập nhật danh sách dịch vụ
                    services = updatedServices

                    // In lại chi tiết sau khi cập nhật
                    println("Services sau khi cập nhật:")
                    services.forEachIndexed { index, service ->
                        println("Updated Service[$index]: ID=${service.serviceId}, ServiceDetailId=${service.serviceDetailId}")
                    }
                }

                uiState = UiState.Success("Lấy thông tin thợ trang điểm thành công")
            } catch (e: HttpException) {
                uiState = UiState.Error("Lỗi HTTP: ${e.message()}")
            } catch (e: IOException) {
                uiState = UiState.Error("Lỗi mạng: ${e.message}")
            } finally {
                isLoading = false
            }
        }
    }

    fun fetchAllServices() {
        viewModelScope.launch {
            isLoading = true
            try {
                val response = generalApi.getAllServices()
                allServices = response
            } catch (e: Exception) {
                // handle error, optionally update uiState
            } finally {
                isLoading = false
            }
        }
    }

    sealed class UiState {
        object Idle : UiState()
        data class Success(val message: String) : UiState()
        data class Error(val message: String) : UiState()
    }
}