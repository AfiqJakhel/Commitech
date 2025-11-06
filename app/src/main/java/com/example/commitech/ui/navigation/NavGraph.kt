package com.example.commitech.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.example.commitech.ui.screen.*
import com.example.commitech.ui.screens.jadwal.UbahJadwalScreen
import com.example.commitech.ui.viewmodel.*

@Composable
fun AppNavGraph() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "splash"
    ) {

        // 🌀 Splash Screen
        composable("splash") {
            SplashScreen(
                onSplashFinished = {
                    navController.navigate("landing") {
                        popUpTo("splash") { inclusive = true }
                    }
                }
            )
        }

        // 🏠 Landing Screen
        composable("landing") {
            LandingScreen(
                onLoginClick = { navController.navigate("login") },
                onSignUpClick = { navController.navigate("register") }
            )
        }

        // 🔐 Login
        composable("login") {
            LoginScreen(
                onBackClick = { navController.popBackStack() },
                onLoginClick = {
                    navController.navigate("home") {
                        popUpTo("landing") { inclusive = true }
                    }
                },
                onForgotPassword = { },
                onSignUpClick = { navController.navigate("register") }
            )
        }

        // 📝 Register
        composable("register") {
            SignUpScreen(
                onBackClick = { navController.popBackStack() },
                onLoginClick = { navController.navigate("login") },
                onSignUpClick = { navController.navigate("login") }
            )
        }

        // 🏡 Home
        composable("home") {
            HomeScreen(
                navController = navController,
                onDataPendaftarClick = { navController.navigate("dataPendaftar") },
                onSeleksiBerkasClick = { navController.navigate("seleksiBerkas") },
                onIsiJadwalClick = { navController.navigate("jadwal_graph") },
                onSeleksiWawancaraClick = { navController.navigate("seleksiWawancara") },
                onKelulusanClick = { navController.navigate("pengumumanKelulusan") }
            )
        }

        // 📄 Data Pendaftar
        composable("dataPendaftar") {
            val viewModel: DataPendaftarViewModel = viewModel()
            DataPendaftarScreen(viewModel = viewModel, onBackClick = { navController.popBackStack() })
        }

        // 📋 Seleksi Berkas
        composable("seleksiBerkas") {
            val viewModel: SeleksiBerkasViewModel = viewModel()
            SeleksiBerkasScreen(viewModel = viewModel, onBackClick = { navController.popBackStack() })
        }

        // 💬 Seleksi Wawancara
        composable("seleksiWawancara") {
            val viewModel: SeleksiWawancaraViewModel = viewModel()
            SeleksiWawancaraScreen(viewModel = viewModel, onBackClick = { navController.popBackStack() })
        }

        // 📢 Pengumuman Kelulusan
        composable("pengumumanKelulusan") {
            val viewModel = viewModel<PengumumanViewModel>()
            PengumumanScreen(navController = navController, viewModel = viewModel)
        }

        // ✏️ Ubah Detail Peserta
        composable(
            route = "ubahDetail/{namaPeserta}",
            arguments = listOf(navArgument("namaPeserta") { type = NavType.StringType })
        ) { backStackEntry ->
            val namaPeserta = backStackEntry.arguments?.getString("namaPeserta") ?: ""
            val viewModel = viewModel<PengumumanViewModel>()
            UbahDetailScreen(navController, namaPeserta, viewModel)
        }

        // 📅 Graph Jadwal Rekrutmen
        navigation(
            startDestination = "jadwalRekrutmen",
            route = "jadwal_graph"
        ) {
            // ✅ Shared ViewModel untuk semua halaman jadwal
            composable("jadwalRekrutmen") { backStackEntry ->
                val parentEntry = remember(backStackEntry) {
                    navController.getBackStackEntry("jadwal_graph")
                }
                val sharedViewModel: JadwalViewModel = viewModel(parentEntry)
                JadwalRekrutmenScreen(
                    navController = navController,
                    viewModel = sharedViewModel
                )
            }

            composable("tambahJadwal") { backStackEntry ->
                val parentEntry = remember(backStackEntry) {
                    navController.getBackStackEntry("jadwal_graph")
                }
                val sharedViewModel: JadwalViewModel = viewModel(parentEntry)
                TambahJadwalScreen(
                    navController = navController,
                    viewModel = sharedViewModel
                )
            }

            composable(
                route = "ubahJadwal/{jadwalId}",
                arguments = listOf(navArgument("jadwalId") { type = NavType.IntType })
            ) { backStackEntry ->
                val parentEntry = remember(backStackEntry) {
                    navController.getBackStackEntry("jadwal_graph")
                }
                val sharedViewModel: JadwalViewModel = viewModel(parentEntry)
                val id = backStackEntry.arguments?.getInt("jadwalId") ?: return@composable
                UbahJadwalScreen(navController, sharedViewModel, id)

            }
            composable("notifikasi") {
                val jadwalViewModel: JadwalViewModel = viewModel()
                NotifikasiScreen(navController = navController, viewModel = jadwalViewModel)
            }

        }

    }
}
