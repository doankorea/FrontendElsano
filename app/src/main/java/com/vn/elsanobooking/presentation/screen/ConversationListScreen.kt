package com.vn.elsanobooking.presentation.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.vn.elsanobooking.config.Constants
import com.vn.elsanobooking.data.models.MessagesUsersListViewModel
import com.vn.elsanobooking.data.models.User
import com.vn.elsanobooking.ui.theme.BluePrimary
import com.vn.elsanobooking.viewModel.ChatViewModel

data class Conversation(
    val user: User,
    val lastMessage: String,
    val timestamp: String,
    val unreadCount: Int = 0
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConversationListScreen(
    modifier: Modifier = Modifier,
    viewModel: ChatViewModel = viewModel(),
    onConversationClick: (Int) -> Unit = {}
) {
    val users by viewModel.users.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var filteredUsers by remember { mutableStateOf(emptyList<MessagesUsersListViewModel>()) }

    // Filter users when searchQuery changes
    LaunchedEffect(searchQuery, users) {
        filteredUsers = if (searchQuery.isEmpty()) {
            users
        } else {
            users.filter { 
                it.userName.contains(searchQuery, ignoreCase = true) || 
                (it.lastMessage?.contains(searchQuery, ignoreCase = true) ?: false)
            }
        }
    }

    LaunchedEffect(Unit) {
        viewModel.loadUsers()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Tin nhắn", fontWeight = FontWeight.Bold) }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Search bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                placeholder = { Text("Tìm kiếm cuộc trò chuyện...") },
                shape = RoundedCornerShape(24.dp),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedBorderColor = BluePrimary,
                    unfocusedBorderColor = Color.Gray
                )
            )

            // Loading indicator
            if (isLoading && users.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } 
            // Error message
            else if (error != null && users.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text("Không thể tải cuộc trò chuyện", color = Color.Red)
                        Button(
                            onClick = { viewModel.loadUsers() },
                            modifier = Modifier.padding(top = 8.dp)
                        ) {
                            Text("Thử lại")
                        }
                    }
                }
            }
            // Empty state
            else if (filteredUsers.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        if (searchQuery.isEmpty()) "Không có cuộc trò chuyện nào" 
                        else "Không tìm thấy cuộc trò chuyện phù hợp",
                        color = Color.Gray
                    )
                }
            } 
            // Conversation list
            else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(filteredUsers) { user ->
                        val conversation = Conversation(
                            user = User(
                                id = user.id,
                                name = user.userName,
                                isActive = true,
                                avatar = user.avatar
                            ),
                            lastMessage = user.lastMessage ?: "Bắt đầu cuộc trò chuyện",
                            timestamp = user.lastMessageDate ?: "Vừa xong",
                            unreadCount = 0
                        )

                        ConversationItem(
                            conversation = conversation,
                            onClick = { onConversationClick(user.id) }
                        )
                    }
                }
            }
        }
    }

    // Show error as Snackbar if needed
    error?.let { errorMessage ->
        Snackbar(
            modifier = Modifier.padding(16.dp),
            action = {
                TextButton(onClick = { /* Clear error */ }) {
                    Text("Đóng")
                }
            }
        ) {
            Text(errorMessage)
        }
    }
}

@Composable
fun ConversationItem(
    conversation: Conversation,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Avatar
        AsyncImage(
            model = conversation.user.avatar?.let { "${Constants.BASE_URL}$it" } ?: "",
            contentDescription = "${conversation.user.name} avatar",
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(Color.Gray),
            placeholder = painterResource(id = android.R.drawable.ic_menu_gallery),
            error = painterResource(id = android.R.drawable.ic_menu_gallery)
        )

        Spacer(modifier = Modifier.width(16.dp))

        // User info and last message
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(end = 8.dp)
        ) {
            Text(
                text = conversation.user.name,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )

            Text(
                text = conversation.lastMessage,
                fontSize = 14.sp,
                color = Color.Gray,
                maxLines = 1
            )
        }

        // Timestamp and unread count
        Column(
            horizontalAlignment = Alignment.End
        ) {
            Text(
                text = conversation.timestamp,
                fontSize = 12.sp,
                color = Color.Gray
            )

            if (conversation.unreadCount > 0) {
                Box(
                    modifier = Modifier
                        .padding(top = 4.dp)
                        .size(24.dp)
                        .background(BluePrimary, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = conversation.unreadCount.toString(),
                        color = Color.White,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }

    Divider(
        modifier = Modifier.padding(start = 72.dp, end = 16.dp),
        color = Color.LightGray,
        thickness = 0.5.dp
    )
}

@Preview(showBackground = true)
@Composable
fun ConversationListScreenPreview() {
    ConversationListScreen()
} 