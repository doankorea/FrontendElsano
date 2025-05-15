package com.vn.elsanobooking.viewModel

import android.content.SharedPreferences
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.edit
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vn.elsanobooking.data.api.Compound
import com.vn.elsanobooking.data.api.RetrofitInstance
import com.vn.elsanobooking.data.api.Suggestion
import com.vn.elsanobooking.data.models.*
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import retrofit2.HttpException
import java.io.File
import java.time.OffsetDateTime

class AuthViewModel(
    private val preferences: SharedPreferences
) : ViewModel() {
    // State variables for user input and profile data
    var userName by mutableStateOf(preferences.getString("userName", "") ?: "")
    var email by mutableStateOf(preferences.getString("email", "") ?: "")
    var password by mutableStateOf("")
    var phoneNumber by mutableStateOf(preferences.getString("phoneNumber", "") ?: "")
    var userId by mutableStateOf(preferences.getInt("userId", -1))
    var avatar by mutableStateOf(preferences.getString("avatar", null))
    var isActive by mutableStateOf(preferences.getInt("isActive", 0))
    var locationId by mutableStateOf(preferences.getInt("locationId", -1).takeIf { it != -1 })
    var location by mutableStateOf<LocationResponse?>(null)
    var createdAt by mutableStateOf(preferences.getString("createdAt", "") ?: "")
    var updatedAt by mutableStateOf(preferences.getString("updatedAt", "") ?: "")
    var isLoading by mutableStateOf(false)
    var isLoginMode by mutableStateOf(true)
    var isLoggedIn by mutableStateOf(preferences.getBoolean("isLoggedIn", false))
    var uiState by mutableStateOf<UiState>(UiState.Idle)
    var requiresEmailConfirmation by mutableStateOf(false)

    private val authApi = RetrofitInstance.authApi

    init {
        Log.d(TAG, "Initializing with isLoggedIn: $isLoggedIn, userId: $userId")
        if (isLoggedIn && userId != -1) {
            fetchUserInfo(userId)
        }
    }

    fun login() {
        Log.d(TAG, "[DEBUG] login() called with userName='$userName', email='$email', password='${password.replace(Regex("."), "*")}'")
        try {
            if (userName.isBlank() || password.isBlank()) {
                uiState = UiState.Error("Vui lòng nhập tên đăng nhập và mật khẩu")
                return
            }
            viewModelScope.launch {
                try {
                    isLoading = true
                    uiState = UiState.Idle
                    try {
                        Log.d(TAG, "[DEBUG] Logging in with userName: $userName, email: $email, password length: ${password.length}")
                        val loginRequest = LoginRequest(userName = userName, password = password)
                        val response = authApi.loginUser(loginRequest)
                        Log.d(TAG, "[DEBUG] loginUser API response: $response")
                        if (response.status == "Success") {
                            val user = response.user ?: throw IllegalStateException("User data missing in response: $response")
                            val userIdFromApi = user.userId ?: throw IllegalStateException("User ID not provided in response: $response")
                            Log.d(TAG, "[DEBUG] Login success, userIdFromApi=$userIdFromApi, userName=${user.userName}, email=${user.email}")
                            preferences.edit {
                                putBoolean("isLoggedIn", true)
                                putString("userName", user.userName)
                                putString("email", user.email)
                                putInt("userId", userIdFromApi)
                            }
                            userId = userIdFromApi
                            userName = user.userName ?: userName
                            email = user.email ?: email
                            isLoggedIn = true
                            fetchUserInfo(userId)
                            uiState = UiState.Success("Đăng nhập thành công")
                            requiresEmailConfirmation = false
                        } else {
                            Log.w(TAG, "[DEBUG] Login failed: ${response.message}")
                            uiState = UiState.Error(response.message ?: "Thông tin đăng nhập không đúng")
                        }
                    } catch (e: HttpException) {
                        Log.e(TAG, "[DEBUG] HttpException during login", e)
                        try {
                            val errorBody = e.response()?.errorBody()
                            val errorString = errorBody?.string()
                            Log.d(TAG, "[DEBUG] Login error response: $errorString")
                            if (errorString != null) {
                                handleLoginError(errorString, e)
                            } else {
                                handleHttpException(e, "login")
                            }
                        } catch (parseEx: Exception) {
                            Log.e(TAG, "[DEBUG] Error parsing error response", parseEx)
                            handleHttpException(e, "login")
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "[DEBUG] Connection error during login", e)
                        uiState = UiState.Error("Không thể kết nối đến máy chủ: ${e.message}")
                    } finally {
                        isLoading = false
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "[DEBUG] Uncaught exception in login coroutine", e)
                    isLoading = false
                    uiState = UiState.Error("Lỗi không xác định: ${e.message}")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "[DEBUG] Fatal exception in login function", e)
            uiState = UiState.Error("Lỗi nghiêm trọng: ${e.message}")
        }
    }

    private fun handleLoginError(errorString: String, e: HttpException) {
        try {
            Log.d(TAG, "[DEBUG] Handling login error: $errorString")
            if (errorString.contains("RequireEmailConfirmation") || 
                errorString.contains("chưa được xác nhận email")) {
                try {
                    val emailRegex = "\"Email\":\"([^\"]+)\"".toRegex()
                    val emailMatch = emailRegex.find(errorString)
                    val extractedEmail = emailMatch?.groupValues?.getOrNull(1)
                    
                    email = extractedEmail ?: ""
                    requiresEmailConfirmation = true
                    uiState = UiState.Error("Tài khoản chưa được xác nhận email. Vui lòng kiểm tra hộp thư của bạn.")
                    Log.d(TAG, "[DEBUG] Set requiresEmailConfirmation=true, email=$email")
                } catch (ex: Exception) {
                    Log.e(TAG, "[DEBUG] Error when setting requiresEmailConfirmation", ex)
                    requiresEmailConfirmation = true
                    email = ""
                    uiState = UiState.Error("Tài khoản chưa được xác nhận email. Vui lòng kiểm tra hộp thư của bạn.")
                }
            } else {
                handleHttpException(e, "login")
            }
        } catch (ex: Exception) {
            Log.e(TAG, "[DEBUG] Error in handleLoginError", ex)
            uiState = UiState.Error("Lỗi khi xử lý phản hồi từ server: ${ex.message}")
        }
    }

    fun register() {
        if (userName.isBlank() || email.isBlank() || password.isBlank()) {
            uiState = UiState.Error("Vui lòng nhập đầy đủ thông tin")
            return
        }
        viewModelScope.launch {
            isLoading = true
            uiState = UiState.Idle
            try {
                Log.d(TAG, "Registering with userName: $userName, email: $email")
                
                // Sử dụng API endpoint /Account/CreateUser
                val registerRequest = RegisterRequest(
                    UserName = userName,
                    Email = email,
                    Password = password
                )
                val response = authApi.createUser(registerRequest)
                
                if (response.status == "Success") {
                    // Xử lý trường hợp đăng ký thành công
                    val userIdFromApi = response.userId ?: throw IllegalStateException("User ID not provided")
                    
                    if (response.requireEmailConfirmation == true) {
                        // Thiết lập trạng thái để hiển thị dialog xác nhận email
                        requiresEmailConfirmation = true
                        email = response.email ?: email
                        uiState = UiState.Success(response.message ?: "Đăng ký thành công. Vui lòng kiểm tra email để xác nhận tài khoản.")
                    } else {
                        // Lưu thông tin người dùng nếu không cần xác nhận email
                        preferences.edit {
                            putBoolean("isLoggedIn", true)
                            putString("userName", userName)
                            putString("email", email)
                            putInt("userId", userIdFromApi)
                        }
                        userId = userIdFromApi
                        isLoggedIn = true
                        fetchUserInfo(userId)
                        uiState = UiState.Success("Tạo tài khoản thành công")
                    }
                } else {
                    val errorBody = response.status
                    Log.e("Register", "HTTP error during register: ${response.message}\nErrorBody: $errorBody")
                    uiState = UiState.Error("Đăng ký thất bại: $errorBody")
                }
            } catch (e: HttpException) {
                val errorBody = e.response()?.errorBody()?.string()
                Log.e("Register", "HTTP error during register: ${e.message()}\nErrorBody: $errorBody")
                uiState = UiState.Error("Đăng ký thất bại: ${errorBody ?: e.message()}")
            } catch (e: Exception) {
                uiState = UiState.Error("Không thể kết nối đến máy chủ: ${e.message}")
                Log.e(TAG, "Connection error during register", e)
            } finally {
                isLoading = false
            }
        }
    }

    fun resendConfirmationEmail(emailToResend: String? = null) {
        val targetEmail = emailToResend ?: email
        if (targetEmail.isBlank()) {
            uiState = UiState.Error("Email không hợp lệ")
            return
        }

        viewModelScope.launch {
            isLoading = true
            uiState = UiState.Idle
            try {
                val response = authApi.resendConfirmationEmail(ResendEmailRequest(email = targetEmail))
                if (response.status == "Success") {
                    uiState = UiState.Success(response.message ?: "Email xác nhận đã được gửi lại. Vui lòng kiểm tra hộp thư của bạn.")
                } else {
                    uiState = UiState.Error(response.message ?: "Không thể gửi lại email xác nhận")
                }
            } catch (e: HttpException) {
                try {
                    val errorBody = e.response()?.errorBody()
                    val errorString = errorBody?.string()
                    if (errorString != null && errorString.contains("đã được xác nhận")) {
                        uiState = UiState.Error("Email này đã được xác nhận trước đó.")
                    } else {
                        handleHttpException(e, "resend confirmation email")
                    }
                } catch (parseEx: Exception) {
                    handleHttpException(e, "resend confirmation email")
                }
            } catch (e: Exception) {
                uiState = UiState.Error("Không thể kết nối đến máy chủ: ${e.message}")
            } finally {
                isLoading = false
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            isLoading = true
            uiState = UiState.Idle
            try {
                Log.d(TAG, "Logging out userId: $userId")
                val response = authApi.logoutUser()
                if (response.Status == "Success") {
                    clearUserData()
                    uiState = UiState.Success("Đăng xuất thành công")
                } else {
                    uiState = UiState.Error("Đăng xuất thất bại")
                }
            } catch (e: HttpException) {
                handleHttpException(e, "logout")
                // Ensure logout even on network error
                clearUserData()
            } catch (e: Exception) {
                uiState = UiState.Error("Không thể kết nối đến máy chủ: ${e.message}")
                Log.e(TAG, "Connection error during logout", e)
                // Ensure logout even on network error
                clearUserData()
            } finally {
                isLoading = false
            }
        }
    }

    fun fetchUserInfo(userId: Int) {
        viewModelScope.launch {
            isLoading = true
            uiState = UiState.Idle
            try {
                Log.d(TAG, "Fetching user info for userId: $userId")
                val response = authApi.getUser(userId)
                Log.d(TAG, "Fetched user info response: id=${response.id}, userName=${response.userName}, isActive=${response.isActive}, location=${response.location}")
                updateUserInfoFromResponse(response)
                preferences.edit {
                    putString("userName", response.userName)
                    putString("email", response.email)
                    putString("phoneNumber", response.phoneNumber)
                    putString("avatar", response.avatar)
                    putInt("isActive", (response.isActive))
                    putInt("locationId", response.locationId ?: -1)
                }
            } catch (e: HttpException) {
                handleHttpException(e, "fetch user info")
            } catch (e: Exception) {
                uiState = UiState.Error("Không thể kết nối đến máy chủ: ${e.message}")
                Log.e(TAG, "Connection error during fetch user info", e)
            } finally {
                isLoading = false
            }
        }
    }

    fun updateUserInfo(
        newUserName: String,
        newPhoneNumber: String,
        newEmail: String,
        newAvatar: String?,
        newIsActive: Int
    ) {
        viewModelScope.launch {
            isLoading = true
            uiState = UiState.Idle
            try {
                Log.d(TAG, "Updating user info for userId: $userId")
                val response = authApi.updateUser(
                    userId,
                    UpdateUserRequest(
                        userName = newUserName,
                        email = newEmail,
                        phoneNumber = newPhoneNumber,
                        avatar = newAvatar,
                        isActive = newIsActive
                    )
                )
                userName = newUserName
                phoneNumber = newPhoneNumber
                email = newEmail
                avatar = newAvatar
                isActive = newIsActive
                preferences.edit {
                    putString("userName", userName)
                    putString("phoneNumber", phoneNumber)
                    putString("email", email)
                    putString("avatar", avatar)
                    putInt("isActive", isActive)
                }
                uiState = UiState.Success(response.message ?: "Cập nhật thông tin thành công")
            } catch (e: HttpException) {
                handleHttpException(e, "update user info")
            } catch (e: Exception) {
                uiState = UiState.Error("Không thể kết nối đến máy chủ: ${e.message}")
                Log.e(TAG, "Connection error during update user info", e)
            } finally {
                isLoading = false
            }
        }
    }

    fun addLocation(latitude: Double, longitude: Double, address: String, type: String) {
        viewModelScope.launch {
            isLoading = true
            uiState = UiState.Idle
            try {
                Log.d(TAG, "Adding location for userId: $userId")
                val response = authApi.addLocation(
                    userId,
                    AddLocationRequest(latitude, longitude, address, type)
                )
                fetchUserInfo(userId)
                uiState = UiState.Success(response.message ?: "Thêm địa chỉ thành công")
            } catch (e: HttpException) {
                handleHttpException(e, "add location")
            } catch (e: Exception) {
                uiState = UiState.Error("Không thể kết nối đến máy chủ: ${e.message}")
                Log.e(TAG, "Connection error during add location", e)
            } finally {
                isLoading = false
            }
        }
    }

    fun updateAvatar(file: File) {
        if (userId == -1) {
            Log.e(TAG, "Invalid userId: $userId")
            uiState = UiState.Error("Phiên đăng nhập không hợp lệ, vui lòng đăng nhập lại")
            isLoading = false
            return
        }
        viewModelScope.launch {
            isLoading = true
            uiState = UiState.Idle
            try {
                Log.d(TAG, "Updating avatar for userId: $userId, file: ${file.path}, exists: ${file.exists()}, size: ${file.length()}")
                val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
                val avatarPart = MultipartBody.Part.createFormData("avatar", file.name, requestFile)
                val response = authApi.updateAvatar(userId, avatarPart)
                Log.d(TAG, "Update avatar response: status=${response.status}, avatar=${response.avatar}")
                if (response.status == "Success") {
                    avatar = response.avatar
                    preferences.edit().putString("avatar", avatar).apply()
                    Log.d(TAG, "Avatar updated in preferences: ${response.avatar}")
                    uiState = UiState.Success(response.message ?: "Cập nhật ảnh đại diện thành công")
                } else {
                    uiState = UiState.Error(response.message ?: "Cập nhật ảnh đại diện thất bại")
                }
            } catch (e: HttpException) {
                val errorBody = e.response()?.errorBody()?.string() ?: "Empty response body"
                Log.e(TAG, "HTTP error during update avatar: code=${e.code()}, body=$errorBody", e)
                handleHttpException(e, "update avatar")
            } catch (e: Exception) {
                uiState = UiState.Error("Không thể kết nối đến máy chủ: ${e.message}")
                Log.e(TAG, "Connection error during update avatar", e)
            } finally {
                isLoading = false
            }
        }
    }
    fun clearUiState() {
        uiState = UiState.Idle
    }

    private fun updatePreferencesOnLogin(userId: Int, user: LoginResponse.User) {
        preferences.edit {
            putBoolean("isLoggedIn", true)
            putString("userName", user.userName)
            putString("email", user.email)
            putInt("userId", userId)
            putInt("isActive", isActive?.toInt() ?: 0)
        }
    }

    private fun updateUserInfoFromResponse(response: UserResponse) {
        userName = response.userName
        email = response.email
        phoneNumber = response.phoneNumber ?: ""
        avatar = response.avatar
        isActive = response.isActive
        locationId = response.locationId
        location = response.location
        createdAt = response.createdAt?: ""
        updatedAt = response.updatedAt?: ""
    }

    private fun clearUserData() {
        preferences.edit { clear() }
        isLoggedIn = false
        userId = -1
        userName = ""
        email = ""
        phoneNumber = ""
        avatar = null
        isActive = 0
        locationId = null
        location = null
        createdAt = ""
        updatedAt = ""
        password = ""
    }
    fun searchAddress(query: String, onResult: (List<Suggestion>) -> Unit) {
        viewModelScope.launch {
            try {
                val response = authApi.searchAddress(query)
                if (response.status == "OK") {
                    onResult(response.predictions.map { prediction ->
                        Suggestion(
                            description = prediction.description,
                            place_id = prediction.place_id,
                            compound = prediction.compound?.let {
                                Compound(
                                    province = it.province,
                                    district = it.district,
                                    commune = it.commune
                                )
                            }
                        )
                    })
                } else {
                    onResult(emptyList())
                }
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Error searching address", e)
                onResult(emptyList())
            }
        }
    }

    fun getCoordinates(placeId: String, onResult: (Double?, Double?) -> Unit) {
        viewModelScope.launch {
            try {
                val response = authApi.getCoordinates(placeId)
                onResult(response.latitude, response.longitude)
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Error getting coordinates", e)
                onResult(null, null)
            }
        }
    }
    private fun handleHttpException(e: HttpException, operation: String) {
        val message = when (e.code()) {
            400 -> "Yêu cầu không hợp lệ"
            401 -> "Thông tin đăng nhập không đúng"
            403 -> "Không có quyền truy cập"
            404 -> "Không tìm thấy tài nguyên"
            409 -> "Tài khoản đã tồn tại"
            500 -> "Lỗi máy chủ, vui lòng thử lại sau"
            else -> "Lỗi HTTP: ${e.message()}"
        }
        uiState = UiState.Error(message)
        Log.e(TAG, "HTTP error during $operation: ${e.message()}", e)
    }

    private fun SharedPreferences.getByte(key: String, default: Byte): Byte? {
        return try {
            val value = getInt(key, default.toInt())
            if (value == default.toInt()) null else value.toByte()
        } catch (e: Exception) {
            null
        }
    }

    sealed class UiState {
        object Idle : UiState()
        data class Success(val message: String) : UiState()
        data class Error(val message: String) : UiState()
    }

    companion object {
        private const val TAG = "AuthViewModel"
    }
}