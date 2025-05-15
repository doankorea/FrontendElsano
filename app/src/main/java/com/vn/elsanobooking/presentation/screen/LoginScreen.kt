package com.vn.elsanobooking.presentation.screen

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.vn.elsanobooking.R
import com.vn.elsanobooking.viewModel.AuthViewModel
import retrofit2.HttpException

enum class AuthMode {
    LOGIN, REGISTER
}
@Composable
fun LoginScreen(navController: NavController, authViewModel: AuthViewModel = viewModel()) {
    var email by remember { mutableStateOf(authViewModel.email) }
    var userName by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var authMode by remember { mutableStateOf(AuthMode.LOGIN) }
    var showEmailInLoginMode by remember { mutableStateOf(false) }
    var showEmailConfirmationDialog by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()
    
    LaunchedEffect(authViewModel.email) {
        email = authViewModel.email
    }
    
    LaunchedEffect(authViewModel.requiresEmailConfirmation) {
        if (authViewModel.requiresEmailConfirmation) {
            showEmailInLoginMode = true
            showEmailConfirmationDialog = true
        } else {
            showEmailConfirmationDialog = false
        }
    }

    if (showEmailConfirmationDialog) {
        EmailConfirmationDialog(
            email = email ?: "",
            onDismiss = { 
                authViewModel.requiresEmailConfirmation = false
                showEmailConfirmationDialog = false
            },
            onResendEmail = { inputEmail ->
                authViewModel.resendConfirmationEmail(inputEmail)
            },
            authViewModel = authViewModel
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(scrollState),
        horizontalAlignment = Alignment.Start
    ) {
        IconButton(onClick = { navController.popBackStack() }) {
            Icon(
                painter = painterResource(id = R.drawable.ic_arrow_back),
                contentDescription = "Quay lại",
                tint = Color.Black
            )
        }

        Text(
            text = if (authMode == AuthMode.LOGIN) "Đăng nhập" else "Đăng ký tài khoản",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(16.dp))

        SocialLoginButtons()

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Divider(modifier = Modifier.weight(0.5f))
            Text(" HOẶC ", color = Color.Gray)
            Divider(modifier = Modifier.weight(0.5f))
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = userName,
            onValueChange = { userName = it },
            label = { Text("Tên đăng nhập") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        if (authMode == AuthMode.REGISTER || showEmailInLoginMode) {
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Địa chỉ email") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
        }
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Mật khẩu") },
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                val icon = if (passwordVisible) R.drawable.ic_date else R.drawable.ic_date
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(painter = painterResource(id = icon), contentDescription = "Toggle password")
                }
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                if (!validateInput(authMode, userName, email, password, authViewModel, showEmailInLoginMode)) return@Button
                authViewModel.email = email
                authViewModel.password = password
                authViewModel.userName = userName
                if (authMode == AuthMode.REGISTER) {
                    authViewModel.register()
                } else {
                    authViewModel.login()
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !authViewModel.isLoading
        ) {
            if (authViewModel.isLoading) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
            } else {
                Text(if (authMode == AuthMode.LOGIN) "Đăng nhập" else "Đăng ký", color = Color.White)
            }
        }

        when (val state = authViewModel.uiState) {
            is AuthViewModel.UiState.Success -> {
                if (!authViewModel.requiresEmailConfirmation) {
                    LaunchedEffect(state) {
                        navController.navigate("home_screen") {
                            popUpTo("login") { inclusive = true }
                        }
                        authViewModel.clearUiState()
                    }
                }
                Text(text = state.message, color = Color.Green, modifier = Modifier.padding(top = 8.dp))
            }
            is AuthViewModel.UiState.Error -> {
                Text(text = state.message, color = Color.Red, modifier = Modifier.padding(top = 8.dp))
            }
            else -> {}
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(onClick = {
            authMode = if (authMode == AuthMode.LOGIN) AuthMode.REGISTER else AuthMode.LOGIN
        }) {
            Text(
                if (authMode == AuthMode.LOGIN) "Chưa có tài khoản? Đăng ký"
                else "Đã có tài khoản? Đăng nhập",
                color = Color(0xFF1877F2)
            )
        }
    }
}

@Composable
fun EmailConfirmationDialog(
    email: String,
    onDismiss: () -> Unit,
    onResendEmail: (String) -> Unit,
    authViewModel: AuthViewModel
) {
    Log.d("EmailDialog", "Starting EmailConfirmationDialog with email: $email")
    var inputEmail by remember { mutableStateOf(email) }
    var isResending by remember { mutableStateOf(false) }

    LaunchedEffect(authViewModel.uiState) {
        when (authViewModel.uiState) {
            is AuthViewModel.UiState.Success, is AuthViewModel.UiState.Error -> {
                isResending = false
                Log.d("EmailDialog", "State updated: ${authViewModel.uiState}")
            }
            else -> {}
        }
    }

    Dialog(onDismissRequest = { 
        Log.d("EmailDialog", "Dialog dismiss requested")
        onDismiss() 
    }) {
        Card(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            shape = RoundedCornerShape(16.dp),
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Xác nhận email", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                Spacer(modifier = Modifier.height(16.dp))
                Text("Vui lòng kiểm tra hộp thư email của bạn để xác nhận tài khoản. Nếu bạn chưa nhận được email, bạn có thể yêu cầu gửi lại.", fontSize = 14.sp)
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = inputEmail,
                    onValueChange = { 
                        inputEmail = it 
                        Log.d("EmailDialog", "Email input changed to: $inputEmail")
                    },
                    label = { Text("Email") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))
                when (val state = authViewModel.uiState) {
                    is AuthViewModel.UiState.Success -> {
                        Text(state.message, color = Color.Green, fontSize = 14.sp)
                    }
                    is AuthViewModel.UiState.Error -> {
                        Text(state.message, color = Color.Red, fontSize = 14.sp)
                    }
                    else -> {}
                }
                Spacer(modifier = Modifier.height(24.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    TextButton(onClick = { 
                        Log.d("EmailDialog", "Close button clicked")
                        onDismiss() 
                    }) { 
                        Text("Đóng") 
                    }
                    Button(
                        onClick = {
                            isResending = true
                            onResendEmail(inputEmail)
                        },
                        enabled = !isResending && !authViewModel.isLoading && inputEmail.isNotBlank()
                    ) {
                        if (isResending || authViewModel.isLoading) {
                            CircularProgressIndicator()
                        } else {
                            Text("Gửi lại email")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SocialLoginButtons() {
    Column {
        Button(
            onClick = { /* Facebook login */ },
            colors = ButtonDefaults.buttonColors(containerColor = Color.White),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            border = ButtonDefaults.outlinedButtonBorder
        ) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Image(
                    painter = painterResource(id = R.drawable.ic_facebook),
                    contentDescription = "Facebook",
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Tiếp tục với Facebook", color = Color.Black, modifier = Modifier.weight(1f), textAlign = androidx.compose.ui.text.style.TextAlign.Center)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = { /* Google login */ },
            colors = ButtonDefaults.buttonColors(containerColor = Color.White),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            border = ButtonDefaults.outlinedButtonBorder
        ) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Image(
                    painter = painterResource(id = R.drawable.ic_google),
                    contentDescription = "Google",
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Tiếp tục với Google", color = Color.Black, modifier = Modifier.weight(1f), textAlign = androidx.compose.ui.text.style.TextAlign.Center)
            }
        }
    }
}

fun validateInput(
    mode: AuthMode,
    userName: String,
    email: String,
    password: String,
    authViewModel: AuthViewModel,
    showEmailInLoginMode: Boolean
): Boolean {
    if (userName.isBlank()) {
        authViewModel.uiState = AuthViewModel.UiState.Error("Tên đăng nhập không được để trống")
        return false
    }

    if ((mode == AuthMode.REGISTER || showEmailInLoginMode) && (email.isBlank() || !email.contains("@"))) {
        authViewModel.uiState = AuthViewModel.UiState.Error("Email không hợp lệ")
        return false
    }

    if (password.length < 6) {
        authViewModel.uiState = AuthViewModel.UiState.Error("Mật khẩu phải có ít nhất 6 ký tự")
        return false
    }

    return true
}
