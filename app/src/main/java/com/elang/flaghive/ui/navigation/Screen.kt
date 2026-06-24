package com.elang.flaghive.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String) {
    data object Login : Screen("auth/login")
    data object Register : Screen("auth/register")
    data object Dashboard : Screen("dashboard")
    data object WriteupList : Screen("writeup/list")
    data object WriteupDetail : Screen("writeup/detail/{writeupId}") {
        fun createRoute(writeupId: String) = "writeup/detail/$writeupId"
    }
    data object CreateWriteup : Screen("writeup/create")
    data object EditWriteup : Screen("writeup/edit/{writeupId}") {
        fun createRoute(writeupId: String) = "writeup/edit/$writeupId"
    }
    data object CategoryList : Screen("category/list")
    data object CategoryDetail : Screen("category/detail/{categoryId}") {
        fun createRoute(categoryId: String) = "category/detail/$categoryId"
    }
    data object Search : Screen("search")
    data object Bookmark : Screen("bookmark")
    data object Profile : Screen("profile")
    data object AdminDashboard : Screen("admin")
    data object ManageUsers : Screen("admin/users")
    data object ManageCategories : Screen("admin/categories")
    data object ManageWriteups : Screen("admin/writeups")
}

data class BottomNavItem(
    val label: String,
    val icon: ImageVector,
    val screen: Screen
)

val bottomNavItems = listOf(
    BottomNavItem("Home", Icons.Filled.Home, Screen.Dashboard),
    BottomNavItem("Search", Icons.Filled.Search, Screen.Search),
    BottomNavItem("Bookmark", Icons.Filled.Bookmark, Screen.Bookmark),
    BottomNavItem("Profile", Icons.Filled.Person, Screen.Profile)
)
