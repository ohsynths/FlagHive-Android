package com.elang.flaghive

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.rememberNavController
import com.elang.flaghive.data.firebase.SeedData
import com.elang.flaghive.data.model.UserRole
import com.elang.flaghive.data.repository.AuthRepository
import com.elang.flaghive.data.repository.UserRepository
import com.elang.flaghive.util.Resource
import com.elang.flaghive.ui.navigation.NavGraph
import com.elang.flaghive.ui.navigation.bottomNavItems
import com.elang.flaghive.ui.navigation.Screen
import com.elang.flaghive.ui.theme.FlagHiveTheme
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.currentBackStackEntryAsState
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.navigation.NavGraph.Companion.findStartDestination
import com.google.firebase.firestore.FirebaseFirestore

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var authRepository: AuthRepository

    @Inject
    lateinit var userRepository: UserRepository

    @Inject
    lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FlagHiveTheme {
                FlagHiveApp(authRepository, userRepository, firestore)
            }
        }
    }
}

@Composable
fun FlagHiveApp(
    authRepository: AuthRepository,
    userRepository: UserRepository,
    firestore: FirebaseFirestore
) {
    val navController = rememberNavController()
    val isLoggedIn = authRepository.isLoggedIn
    var isAdmin by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        SeedData.seedIfEmpty(firestore)
    }

    LaunchedEffect(isLoggedIn) {
        if (isLoggedIn) {
            when (val result = userRepository.getUserProfile(authRepository.getCurrentUserId())) {
                is Resource.Success -> isAdmin = result.data.role == UserRole.ADMIN
                else -> isAdmin = false
            }
        }
    }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val showBottomBar = currentDestination?.route in listOf(
        Screen.Dashboard.route,
        Screen.Search.route,
        Screen.Bookmark.route,
        Screen.Profile.route
    )

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    bottomNavItems.take(2).forEach { item ->
                        NavigationBarItem(
                            icon = { Icon(item.icon, contentDescription = item.label) },
                            label = { Text(item.label) },
                            selected = currentDestination?.hierarchy?.any {
                                it.route == item.screen.route
                            } == true,
                            onClick = {
                                navController.navigate(item.screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }

                    NavigationBarItem(
                        icon = { Icon(Icons.Filled.Add, contentDescription = "New Writeup") },
                        label = { Text("New") },
                        selected = false,
                        onClick = {
                            navController.navigate(Screen.CreateWriteup.route) {
                                launchSingleTop = true
                            }
                        }
                    )

                    bottomNavItems.drop(2).forEach { item ->
                        NavigationBarItem(
                            icon = { Icon(item.icon, contentDescription = item.label) },
                            label = { Text(item.label) },
                            selected = currentDestination?.hierarchy?.any {
                                it.route == item.screen.route
                            } == true,
                            onClick = {
                                navController.navigate(item.screen.route) {
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
        }
    ) { innerPadding ->
        NavGraph(
            navController = navController,
            isLoggedIn = isLoggedIn,
            isAdmin = isAdmin
        )
    }
}
