package com.vn.elsanobooking.presentation.screen

import android.content.SharedPreferences
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.preference.PreferenceManager
import com.vn.elsanobooking.navigation.screen.BottomNavItemScreen
import com.vn.elsanobooking.ui.theme.BluePrimary
import com.vn.elsanobooking.ui.theme.PurpleGrey
import com.vn.elsanobooking.ui.theme.poppinsFontFamily
import com.vn.elsanobooking.viewModel.AuthViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.vn.elsanobooking.viewModel.ChatViewModel

@Composable
fun MainScreen(modifier: Modifier = Modifier) {
    var navigationSelectedItem by remember { mutableIntStateOf(0) }
    val bottomNavigationItems = listOf(
        BottomNavItemScreen.Home,
        BottomNavItemScreen.Search,
        BottomNavItemScreen.Schedule,
        BottomNavItemScreen.Chat,
        BottomNavItemScreen.Profile
    )

    val navController = rememberNavController()
    LaunchedEffect(navController) {
        navController.currentBackStackEntryFlow.collect { backStackEntry ->
            val currentRoute = backStackEntry.destination.route
            val selectedIndex = bottomNavigationItems.indexOfFirst { it.route == currentRoute }
            if (selectedIndex != -1) {
                navigationSelectedItem = selectedIndex
            }
        }
    }

    // Share AuthViewModel instance across screens
    val preferences = PreferenceManager.getDefaultSharedPreferences(
        androidx.compose.ui.platform.LocalContext.current
    )
    val sharedViewModel = remember { AuthViewModel(preferences) }
    var userId by remember { mutableStateOf(preferences.getInt("userId", -1)) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar(
                containerColor = Color.White,
                tonalElevation = 8.dp
            ) {
                bottomNavigationItems.forEachIndexed { index, bottomNavItemScreen ->
                    NavigationBarItem(
                        colors = NavigationBarItemDefaults.colors(
                            selectedTextColor = BluePrimary,
                            selectedIconColor = BluePrimary,
                            unselectedTextColor = PurpleGrey,
                            unselectedIconColor = PurpleGrey
                        ),
                        selected = index == navigationSelectedItem,
                        icon = {
                            Icon(
                                painter = painterResource(id = bottomNavItemScreen.icon),
                                contentDescription = "${bottomNavItemScreen.title} Icon"
                            )
                        },
                        label = {
                            Text(
                                text = bottomNavItemScreen.title,
                                fontFamily = poppinsFontFamily,
                                fontWeight = FontWeight.Medium
                            )
                        },
                        alwaysShowLabel = index == navigationSelectedItem,
                        onClick = {
                            navigationSelectedItem = index
                            navController.navigate(bottomNavItemScreen.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { paddingValues ->
        NavHost(
            modifier = Modifier.padding(paddingValues = paddingValues),
            navController = navController,
            startDestination = BottomNavItemScreen.Home.route
        ) {
            composable(route = BottomNavItemScreen.Home.route) {
                HomeScreen(
                    onArtistClick = { artistId ->
                        navController.navigate("artist_detail/$artistId/$userId")
                    }
                )
            }
            composable(route = BottomNavItemScreen.Search.route) {
                SearchScreen(navController = navController, userId = userId)
            }
            composable(route = "artist_detail/{artistId}/{userId}") { backStackEntry ->
                val artistId = backStackEntry.arguments?.getString("artistId")?.toIntOrNull() ?: 0
                val userId = backStackEntry.arguments?.getString("userId")?.toIntOrNull() ?: 0
                ArtistDetailScreen(
                    artistId = artistId,
                    userId = userId,
                    onBackClick = { navController.popBackStack() },
                    onNavigateToSchedule = {
                        navController.navigate(BottomNavItemScreen.Schedule.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    onNavigateToChat = { artistId ->
                        navController.navigate("chat_with_artist/$artistId")
                    }
                )
            }
            composable(route = BottomNavItemScreen.Schedule.route) {
                ScheduleScreen(
                    onNavigateToChat = { artistId ->
                        navController.navigate("chat_with_artist/$artistId")
                    }
                )
            }
            composable(route = BottomNavItemScreen.Chat.route) {
                val chatViewModel: ChatViewModel = viewModel()
                ConversationListScreen(
                    onConversationClick = { userId ->
                        navController.navigate("chat_with_user/$userId")
                    },
                    viewModel = chatViewModel
                )
            }
            composable(route = BottomNavItemScreen.Profile.route) {
                if (sharedViewModel.isLoggedIn) {
                    ProfileScreen(navController, sharedViewModel)
                } else {
                    LoginScreen(navController, sharedViewModel)
                }
            }
            composable(route = "chat_with_artist/{artistId}") { backStackEntry ->
                val artistId = backStackEntry.arguments?.getString("artistId")?.toIntOrNull() ?: 0
                val chatViewModel: ChatViewModel = viewModel()
                val users by chatViewModel.users.collectAsState()
                val artistUser = users.find { it.id == artistId }
                val artistName = artistUser?.userName ?: "Nghệ sĩ #$artistId"
                val artistAvatar = artistUser?.avatar

                // Luôn load users nếu chưa có
                LaunchedEffect(users.isEmpty()) {
                    if (users.isEmpty()) {
                        chatViewModel.loadUsers()
                    }
                }

                // Chỉ setup chat khi đã có artistUser
                LaunchedEffect(artistUser) {
                    if (artistId > 0 && artistUser != null) {
                        chatViewModel.setupChatWithArtist(artistId, artistName, artistAvatar)
                    }
                }

                ChatScreen(
                    onBackClick = { navController.popBackStack() },
                    viewModel = chatViewModel,
                    showUserList = false
                )
            }
            composable(route = "chat_with_user/{userId}") { backStackEntry ->
                val userId = backStackEntry.arguments?.getString("userId")?.toIntOrNull() ?: 0
                val chatViewModel: ChatViewModel = viewModel()
                val users by chatViewModel.users.collectAsState()
                val user = users.find { it.id == userId }
                val userName = user?.userName ?: "User #$userId"
                val userAvatar = user?.avatar

                // Luôn load users nếu chưa có
                LaunchedEffect(users.isEmpty()) {
                    if (users.isEmpty()) {
                        chatViewModel.loadUsers()
                    }
                }

                // Chỉ setup chat khi đã có user
                LaunchedEffect(user) {
                    if (userId > 0 && user != null) {
                        chatViewModel.setupChatWithUser(userId, userName, userAvatar)
                    } else if (userId > 0) {
                        chatViewModel.setupChatWithUser(userId, userName,userAvatar)
                    }
                }

                ChatScreen(
                    onBackClick = { navController.popBackStack() },
                    viewModel = chatViewModel,
                    showUserList = false
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MainScreenPreview() {
    MainScreen()
}