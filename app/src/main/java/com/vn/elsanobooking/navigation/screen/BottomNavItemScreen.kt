package com.vn.elsanobooking.navigation.screen

import com.vn.elsanobooking.R

sealed class BottomNavItemScreen(val route: String, val icon: Int, val title: String) {

    data object Home : BottomNavItemScreen(
        route = "home_screen",
        icon = R.drawable.ic_bottom_home,
        title = "Home"
    )
    data object Search : BottomNavItemScreen(
        route = "search_screen",
        icon = R.drawable.ic_search ,
        title = "Search"
    )
    data object Schedule : BottomNavItemScreen(
        route = "schedule_screen",
        icon = R.drawable.ic_bottom_schedule,
        title = "Sche"
    )
    data object Chat : BottomNavItemScreen(
        route = "chat_screen",
        icon = R.drawable.ic_bottom_chat,
        title = "Chat"
    )
    data object Profile : BottomNavItemScreen(
        route = "profile_screen",
        icon = R.drawable.ic_bottom_profile,
        title = "Profile"
    )


}