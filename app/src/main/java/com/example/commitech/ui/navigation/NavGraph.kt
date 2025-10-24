 package com.example.commitech.ui.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.commitech.ui.screen.DataPendaftarScreen
import com.example.commitech.ui.screen.HomeScreen
import com.example.commitech.ui.screen.LandingScreen
import com.example.commitech.ui.screen.LoginScreen
import com.example.commitech.ui.screen.SeleksiWawancaraScreen
import com.example.commitech.ui.screen.SplashScreen
import com.example.commitech.ui.screen.SignUpScreen
import com.example.commitech.ui.viewmodel.DataPendaftarViewModel
import com.example.commitech.ui.viewmodel.SeleksiWawancaraViewModel

@Composable
fun AppNavGraph() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "splash"
    ) {
        composable("splash") {
            SplashScreen(
                onSplashFinished = {
                    navController.navigate("landing") {
                        popUpTo("splash") { inclusive = true }
                    }
                }
            )
        }

        // 🏠 Halaman Landing
        composable("landing") {
            LandingScreen(
                onLoginClick = { navController.navigate("login") },
                onSignUpClick = { navController.navigate("register") }
            )
        }

        // 🔐 Halaman Login
        composable("login") {
            LoginScreen(
                onBackClick = {
                    // 🔙 Arahkan kembali ke halaman Landing
                    navController.navigate("landing") {
                        popUpTo("landing") { inclusive = true }
                    }
                },
                onLoginClick = {
                    navController.navigate("home") {
                        popUpTo("landing") { inclusive = true } // supaya tidak bisa kembali ke landing
                    }
                },
                onForgotPassword = { /* TODO */ },
                onSignUpClick = { navController.navigate("register") }
            )
        }

        // 📝 Halaman SignUp (nanti kamu isi)
        composable("register") {
            SignUpScreen(
                onBackClick = {
                    navController.navigate("landing") {
                        popUpTo("landing") { inclusive = true }
                    }
                },
                onLoginClick = {navController.navigate("login") },
                onSignUpClick = { navController.navigate("login")},
            )
        }

        composable("home") {
            HomeScreen(
                onDataPendaftarClick = {navController.navigate("dataPendaftar")
                },
                onSeleksiBerkasClick = { /* route ke seleksi berkas */ },
                onIsiJadwalClick = { /* route ke jadwal */ },
                onSeleksiWawancaraClick = {
                    navController.navigate("seleksiWawancara")
                },
                onKelulusanClick = { /* route ke kelulusan */ }
            )
        }

        composable("seleksiWawancara") {
            val viewModel: SeleksiWawancaraViewModel = viewModel()
            SeleksiWawancaraScreen(
                viewModel = viewModel,
                onBackClick = { navController.popBackStack() }
            )
        }

        composable("dataPendaftar") {
            val viewModel: DataPendaftarViewModel = viewModel()
            DataPendaftarScreen(
                viewModel = viewModel,
                onBackClick = { navController.popBackStack() }
            )
        }
    }
}
