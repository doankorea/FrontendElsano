@file:OptIn(ExperimentalMaterial3Api::class)

package com.vn.elsanobooking.presentation.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.vn.elsanobooking.R
import com.vn.elsanobooking.data.models.MessagesUsersListViewModel
import com.vn.elsanobooking.viewModel.ChatViewModel

// This file is deprecated. ConversationListScreen has been moved to ConversationListScreen.kt
// This file is kept for reference only, but should not be used.

// Don't use these data classes - they've been moved to ConversationListScreen.kt
// Keeping them here for reference while we migrate
data class ConversationOld(
    val user: UserOld,
    val lastMessage: String,
    val timestamp: String,
    val unreadCount: Int
)

data class UserOld(val name: String, val isActive: Boolean, val avatarColor: Color? = null, val avatar: String? = null)

// Chỉ sử dụng cho Preview
val demoConversationsOld = listOf(
    ConversationOld(
        UserOld("Người A", true, Color(0xFF4CAF50)),
        "Chào bạn, bạn có khỏe không?",
        "10:03",
        2
    ),
    ConversationOld(
        UserOld("Người B", false, Color(0xFF2196F3)),
        "Hẹn gặp bạn tối nay nhé!",
        "09:45",
        0
    ),
    ConversationOld(
        UserOld("Người C", true, Color(0xFFF44336)),
        "Bạn đã hoàn thành chưa?",
        "Hôm qua",
        1
    )
)

// DEPRECATED: Use the version in ConversationListScreen.kt instead
@Composable
private fun ConversationListScreenOld(
    modifier: Modifier = Modifier,
    onConversationClick: (ConversationOld) -> Unit = {},
    viewModel: ChatViewModel = viewModel()
) {
    // Content removed to avoid duplication
    Text("This screen is deprecated. Please use ConversationListScreen from ConversationListScreen.kt")
}

// Use these Composable functions if they're not already in the other file
@Composable
fun ConversationItemOld(
    conversation: ConversationOld,
    onClick: () -> Unit
) {
    // ... existing code ...
}