package com.example.commitech.ui.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.commitech.ui.components.SharedBottomBar

@Composable
fun MainScreen(
    mainNavController: NavHostController,
    onDataPendaftarClick: () -> Unit,
    onSeleksiBerkasClick: () -> Unit,
    onIsiJadwalClick: () -> Unit,
    onSeleksiWawancaraClick: () -> Unit,
    onKelulusanClick: () -> Unit,
    onSettingsClick: () -> Unit
) {
    val bottomNavController = rememberNavController()
    var selectedTab by remember { mutableIntStateOf(0) }

    Scaffold(
        containerColor = androidx.compose.ui.graphics.Color.Transparent,
        bottomBar = {
            SharedBottomBar(
                selectedTab = selectedTab,
                onTabSelected = { tab ->
                    selectedTab = tab
                    when (tab) {
                        0 -> bottomNavController.navigate("home") {
                            popUpTo("home") { inclusive = true }
                        }
                        1 -> bottomNavController.navigate("about") {
                            popUpTo("home")
                        }
                        2 -> {
                            // Profile - belum ada screen
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            NavHost(
                navController = bottomNavController,
                startDestination = "home"
            ) {
                composable("home") {
                    HomeScreen(
                        navController = mainNavController,
                        onDataPendaftarClick = onDataPendaftarClick,
                        onSeleksiBerkasClick = onSeleksiBerkasClick,
                        onIsiJadwalClick = onIsiJadwalClick,
                        onSeleksiWawancaraClick = onSeleksiWawancaraClick,
                        onKelulusanClick = onKelulusanClick,
                        onAboutUsClick = {
                            selectedTab = 1
                            bottomNavController.navigate("about")
                        },
                        onSettingsClick = onSettingsClick
                    )
                }

                composable("about") {
                    AboutUsScreen(
                        onBackClick = {
                            selectedTab = 0
                            bottomNavController.navigate("home") {
                                popUpTo("home") { inclusive = true }
                            }
                        },
                        onHomeClick = {
                            selectedTab = 0
                            bottomNavController.navigate("home") {
                                popUpTo("home") { inclusive = true }
                            }
                        }
                    )
                }
            }
        }
    }
}
