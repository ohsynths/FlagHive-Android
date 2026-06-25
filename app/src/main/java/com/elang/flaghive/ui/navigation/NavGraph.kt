package com.elang.flaghive.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.elang.flaghive.ui.admin.AdminWriteupScreen
import com.elang.flaghive.ui.admin.ManageCategoriesScreen
import com.elang.flaghive.ui.admin.ManageUsersScreen
import com.elang.flaghive.ui.auth.LoginScreen
import com.elang.flaghive.ui.auth.RegisterScreen
import com.elang.flaghive.ui.bookmark.BookmarkScreen
import com.elang.flaghive.ui.category.CategoryDetailScreen
import com.elang.flaghive.ui.dashboard.DashboardScreen
import com.elang.flaghive.ui.profile.ProfileScreen
import com.elang.flaghive.ui.search.SearchScreen
import com.elang.flaghive.ui.writeup.create.CreateWriteupScreen
import com.elang.flaghive.ui.writeup.detail.WriteupDetailScreen
import com.elang.flaghive.ui.writeup.edit.EditWriteupScreen
import com.elang.flaghive.ui.writeup.list.WriteupListScreen

@Composable
fun NavGraph(
    navController: NavHostController,
    isLoggedIn: Boolean,
    isAdmin: Boolean
) {
    val startDestination = if (isLoggedIn) Screen.Dashboard.route else Screen.Login.route

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Screen.Login.route) {
            LoginScreen(navController)
        }
        composable(Screen.Register.route) {
            RegisterScreen(navController)
        }
        composable(Screen.Dashboard.route) {
            DashboardScreen(navController)
        }
        composable(Screen.WriteupList.route) {
            WriteupListScreen(navController)
        }
        composable(
            route = Screen.WriteupDetail.route,
            arguments = listOf(navArgument("writeupId") { type = NavType.StringType })
        ) { backStackEntry ->
            val writeupId = backStackEntry.arguments?.getString("writeupId") ?: return@composable
            WriteupDetailScreen(navController, writeupId)
        }
        composable(Screen.CreateWriteup.route) {
            CreateWriteupScreen(navController)
        }
        composable(
            route = Screen.EditWriteup.route,
            arguments = listOf(navArgument("writeupId") { type = NavType.StringType })
        ) { backStackEntry ->
            val writeupId = backStackEntry.arguments?.getString("writeupId") ?: return@composable
            EditWriteupScreen(navController, writeupId)
        }
        composable(
            route = Screen.CategoryDetail.route,
            arguments = listOf(navArgument("categoryId") { type = NavType.StringType })
        ) { backStackEntry ->
            val categoryId = backStackEntry.arguments?.getString("categoryId") ?: return@composable
            CategoryDetailScreen(navController, categoryId)
        }
        composable(Screen.Search.route) {
            SearchScreen(navController)
        }
        composable(Screen.Bookmark.route) {
            BookmarkScreen(navController)
        }
        composable(Screen.Profile.route) {
            ProfileScreen(navController)
        }
        if (isAdmin) {
            composable(Screen.ManageUsers.route) {
                ManageUsersScreen(navController)
            }
            composable(Screen.ManageCategories.route) {
                ManageCategoriesScreen(navController)
            }
            composable(Screen.ManageWriteups.route) {
                AdminWriteupScreen(navController)
            }
        }
    }
}
