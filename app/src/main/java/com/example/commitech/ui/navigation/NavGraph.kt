package com.example.commitech.ui.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.example.commitech.ui.screen.*
import com.example.commitech.ui.screen.UbahJadwalScreen
import com.example.commitech.ui.viewmodel.*

// Animation constants - Modern Web Style
private const val ANIMATION_DURATION = 400
private const val ANIMATION_DURATION_FAST = 300
private const val ANIMATION_DURATION_SLOW = 500

// 🌊 Modern Web Style: Smooth Slide with Parallax Effect
private fun powerPointPushEnter() = slideInHorizontally(
    initialOffsetX = { it },
    animationSpec = tween(ANIMATION_DURATION, easing = androidx.compose.animation.core.CubicBezierEasing(0.4f, 0.0f, 0.2f, 1.0f))
) + scaleIn(
    initialScale = 0.92f,
    animationSpec = tween(ANIMATION_DURATION, easing = androidx.compose.animation.core.CubicBezierEasing(0.4f, 0.0f, 0.2f, 1.0f))
) + fadeIn(
    animationSpec = tween(ANIMATION_DURATION_FAST, easing = androidx.compose.animation.core.LinearOutSlowInEasing)
)

private fun powerPointPushExit() = slideOutHorizontally(
    targetOffsetX = { -it / 4 },
    animationSpec = tween(ANIMATION_DURATION, easing = androidx.compose.animation.core.CubicBezierEasing(0.4f, 0.0f, 0.2f, 1.0f))
) + scaleOut(
    targetScale = 0.92f,
    animationSpec = tween(ANIMATION_DURATION, easing = androidx.compose.animation.core.CubicBezierEasing(0.4f, 0.0f, 0.2f, 1.0f))
) + fadeOut(
    animationSpec = tween(ANIMATION_DURATION_FAST, easing = androidx.compose.animation.core.FastOutLinearInEasing)
)

private fun powerPointPushPopEnter() = slideInHorizontally(
    initialOffsetX = { -it / 4 },
    animationSpec = tween(ANIMATION_DURATION, easing = androidx.compose.animation.core.CubicBezierEasing(0.4f, 0.0f, 0.2f, 1.0f))
) + scaleIn(
    initialScale = 0.92f,
    animationSpec = tween(ANIMATION_DURATION, easing = androidx.compose.animation.core.CubicBezierEasing(0.4f, 0.0f, 0.2f, 1.0f))
) + fadeIn(
    animationSpec = tween(ANIMATION_DURATION_FAST, easing = androidx.compose.animation.core.LinearOutSlowInEasing)
)

private fun powerPointPushPopExit() = slideOutHorizontally(
    targetOffsetX = { it },
    animationSpec = tween(ANIMATION_DURATION, easing = androidx.compose.animation.core.CubicBezierEasing(0.4f, 0.0f, 0.2f, 1.0f))
) + scaleOut(
    targetScale = 0.92f,
    animationSpec = tween(ANIMATION_DURATION, easing = androidx.compose.animation.core.CubicBezierEasing(0.4f, 0.0f, 0.2f, 1.0f))
) + fadeOut(
    animationSpec = tween(ANIMATION_DURATION_FAST, easing = androidx.compose.animation.core.FastOutLinearInEasing)
)

// ✨ Modern Web Style: Elegant Zoom with Bounce
private fun powerPointZoomEnter() = scaleIn(
    initialScale = 0.88f,
    animationSpec = tween(ANIMATION_DURATION_SLOW, easing = androidx.compose.animation.core.CubicBezierEasing(0.34f, 1.56f, 0.64f, 1.0f))
) + fadeIn(
    animationSpec = tween(ANIMATION_DURATION_FAST, easing = androidx.compose.animation.core.LinearOutSlowInEasing)
)

private fun powerPointZoomExit() = scaleOut(
    targetScale = 1.08f,
    animationSpec = tween(ANIMATION_DURATION_FAST, easing = androidx.compose.animation.core.CubicBezierEasing(0.4f, 0.0f, 1.0f, 1.0f))
) + fadeOut(
    animationSpec = tween(ANIMATION_DURATION_FAST, easing = androidx.compose.animation.core.FastOutLinearInEasing)
)

// 🎭 Modern Web Style: Smooth Fade with Subtle Scale
private fun powerPointFadeEnter() = fadeIn(
    animationSpec = tween(ANIMATION_DURATION, easing = androidx.compose.animation.core.CubicBezierEasing(0.4f, 0.0f, 0.2f, 1.0f))
) + scaleIn(
    initialScale = 0.96f,
    animationSpec = tween(ANIMATION_DURATION, easing = androidx.compose.animation.core.CubicBezierEasing(0.4f, 0.0f, 0.2f, 1.0f))
)

private fun powerPointFadeExit() = fadeOut(
    animationSpec = tween(ANIMATION_DURATION_FAST, easing = androidx.compose.animation.core.FastOutLinearInEasing)
) + scaleOut(
    targetScale = 1.04f,
    animationSpec = tween(ANIMATION_DURATION_FAST, easing = androidx.compose.animation.core.CubicBezierEasing(0.4f, 0.0f, 1.0f, 1.0f))
)

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun AppNavGraph(
    settingsViewModel: SettingsViewModel = viewModel()
) {
    val navController = rememberNavController()
    
    // Shared ViewModels untuk sinkronisasi data
    val authViewModel: AuthViewModel = viewModel()
    val dataPendaftarViewModel: DataPendaftarViewModel = viewModel()
    val seleksiWawancaraViewModel: SeleksiWawancaraViewModel = viewModel()
    val pengumumanViewModel: PengumumanViewModel = viewModel()
    val jadwalViewModel: JadwalViewModel = viewModel()

    NavHost(
        navController = navController,
        startDestination = "splash"
    ) {

        // 🌀 Splash Screen
        composable(
            route = "splash",
            enterTransition = { powerPointFadeEnter() },
            exitTransition = { powerPointFadeExit() }
        ) {
            SplashScreen(
                authViewModel = authViewModel,
                onNavigateToHome = {
                    navController.navigate("home") {
                        popUpTo("splash") { inclusive = true }
                    }
                },
                onNavigateToLanding = {
                    navController.navigate("landing") {
                        popUpTo("splash") { inclusive = true }
                    }
                }
            )
        }

        // 🏠 Landing Screen
        composable(
            route = "landing",
            enterTransition = { powerPointZoomEnter() },
            exitTransition = { powerPointZoomExit() }
        ) {
            LandingScreen(
                onLoginClick = { navController.navigate("login") },
                onSignUpClick = { navController.navigate("register") }
            )
        }

        // 🔐 Login
        composable(
            route = "login",
            enterTransition = { powerPointPushEnter() },
            exitTransition = { powerPointPushExit() },
            popEnterTransition = { powerPointPushPopEnter() },
            popExitTransition = { powerPointPushPopExit() }
        ) {
            LoginScreen(
                authViewModel = authViewModel,
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
        composable(
            route = "register",
            enterTransition = { powerPointPushEnter() },
            exitTransition = { powerPointPushExit() },
            popEnterTransition = { powerPointPushPopEnter() },
            popExitTransition = { powerPointPushPopExit() }
        ) {
            SignUpScreen(
                authViewModel = authViewModel,
                onBackClick = { navController.popBackStack() },
                onLoginClick = { navController.navigate("login") },
                onSignUpClick = { navController.navigate("login") }
            )
        }

        // 🏡 Main (Home + About Us with shared bottom nav)
        composable(
            route = "home",
            enterTransition = { powerPointZoomEnter() },
            exitTransition = { powerPointZoomExit() }
        ) {
            MainScreen(
                mainNavController = navController,
                authViewModel = authViewModel,
                jadwalViewModel = jadwalViewModel,
                dataPendaftarViewModel = dataPendaftarViewModel,
                onDataPendaftarClick = { navController.navigate("dataPendaftar") },
                onSeleksiBerkasClick = { navController.navigate("seleksiBerkas") },
                onIsiJadwalClick = { navController.navigate("jadwal_graph") },
                onSeleksiWawancaraClick = { navController.navigate("seleksiWawancara") },
                onKelulusanClick = { navController.navigate("pengumumanKelulusan") },
                onSettingsClick = { navController.navigate("settings") }
            )
        }

        // ⚙️ Settings (menggunakan shared ViewModel)
        composable(
            route = "settings",
            enterTransition = { powerPointPushEnter() },
            exitTransition = { powerPointPushExit() },
            popEnterTransition = { powerPointPushPopEnter() },
            popExitTransition = { powerPointPushPopExit() }
        ) {
            SettingsScreen(
                authViewModel = authViewModel,
                viewModel = settingsViewModel,
                onBackClick = { navController.popBackStack() },
                onLogoutClick = {
                    authViewModel.logout()
                    navController.navigate("landing") {
                        popUpTo("home") { inclusive = true }
                    }
                }
            )
        }

        // 📄 Data Pendaftar
        composable(
            route = "dataPendaftar",
            enterTransition = { powerPointPushEnter() },
            exitTransition = { powerPointPushExit() },
            popEnterTransition = { powerPointPushPopEnter() },
            popExitTransition = { powerPointPushPopExit() }
        ) {
            val viewModel: DataPendaftarViewModel = viewModel()
            DataPendaftarScreen(
                viewModel = viewModel,
                authViewModel = authViewModel,
                onBackClick = { navController.popBackStack() }
            )
        }

        // 📋 Seleksi Berkas
        composable(
            route = "seleksiBerkas",
            enterTransition = { powerPointPushEnter() },
            exitTransition = { powerPointPushExit() },
            popEnterTransition = { powerPointPushPopEnter() },
            popExitTransition = { powerPointPushPopExit() }
        ) {
            val viewModel: SeleksiBerkasViewModel = viewModel()
            SeleksiBerkasScreen(
                viewModel = viewModel,
                authViewModel = authViewModel,
                onBackClick = { navController.popBackStack() }
            )
        }

        // 💬 Seleksi Wawancara (menggunakan shared ViewModel)
        composable(
            route = "seleksiWawancara",
            enterTransition = { powerPointPushEnter() },
            exitTransition = { powerPointPushExit() },
            popEnterTransition = { powerPointPushPopEnter() },
            popExitTransition = { powerPointPushPopExit() }
        ) {
            SeleksiWawancaraScreen(
                viewModel = seleksiWawancaraViewModel,
                authViewModel = authViewModel,  // Tambahkan authViewModel untuk token API call (Fitur 16)
                onBackClick = { navController.popBackStack() },
                navController = navController,
                jadwalViewModel = jadwalViewModel
            )
        }

        // 📋 Detail Jadwal Wawancara
        composable(
            route = "detailJadwalWawancara/{jadwalId}",
            arguments = listOf(navArgument("jadwalId") { type = NavType.IntType }),
            enterTransition = { powerPointPushEnter() },
            exitTransition = { powerPointPushExit() },
            popEnterTransition = { powerPointPushPopEnter() },
            popExitTransition = { powerPointPushPopExit() }
        ) { backStackEntry ->
            val jadwalId = backStackEntry.arguments?.getInt("jadwalId") ?: return@composable
            val jadwal = jadwalViewModel.getJadwalById(jadwalId) ?: return@composable
            val seleksiBerkasViewModel: SeleksiBerkasViewModel = viewModel()
            DetailJadwalWawancaraScreen(
                navController = navController,
                jadwal = jadwal,
                seleksiBerkasViewModel = seleksiBerkasViewModel,
                jadwalViewModel = jadwalViewModel,
                authViewModel = authViewModel
            )
        }

        // 📢 Pengumuman Kelulusan (menggunakan shared ViewModel)
        composable(
            route = "pengumumanKelulusan",
            enterTransition = { powerPointPushEnter() },
            exitTransition = { powerPointPushExit() },
            popEnterTransition = { powerPointPushPopEnter() },
            popExitTransition = { powerPointPushPopExit() }
        ) {
            PengumumanScreen(
                navController = navController,
                viewModel = pengumumanViewModel,
                seleksiViewModel = seleksiWawancaraViewModel
            )
        }

        // ✏️ Ubah Detail Peserta (menggunakan shared ViewModel)
        composable(
            route = "ubahDetail/{namaPeserta}",
            arguments = listOf(navArgument("namaPeserta") { type = NavType.StringType }),
            enterTransition = { powerPointPushEnter() },
            exitTransition = { powerPointPushExit() },
            popEnterTransition = { powerPointPushPopEnter() },
            popExitTransition = { powerPointPushPopExit() }
        ) { backStackEntry ->
            val namaPeserta = backStackEntry.arguments?.getString("namaPeserta") ?: ""
            UbahDetailScreen(
                navController = navController,
                namaPeserta = namaPeserta,
                viewModel = pengumumanViewModel,
                seleksiViewModel = seleksiWawancaraViewModel
            )
        }

        // 🔔 Notifikasi (menggunakan shared ViewModel)
        composable(
            route = "notifikasi",
            enterTransition = { powerPointPushEnter() },
            exitTransition = { powerPointPushExit() },
            popEnterTransition = { powerPointPushPopEnter() },
            popExitTransition = { powerPointPushPopExit() }
        ) {
            NotifikasiScreen(navController = navController, viewModel = jadwalViewModel)
        }

        // 📅 Graph Jadwal Rekrutmen
        navigation(
            startDestination = "jadwalRekrutmen",
            route = "jadwal_graph"
        ) {
            // ✅ Shared ViewModel untuk semua halaman jadwal
            composable(
                route = "jadwalRekrutmen",
                enterTransition = { powerPointPushEnter() },
                exitTransition = { powerPointPushExit() },
                popEnterTransition = { powerPointPushPopEnter() },
                popExitTransition = { powerPointPushPopExit() }
            ) {
                JadwalRekrutmenScreen(
                    navController = navController,
                    viewModel = jadwalViewModel
                )
            }

            composable(
                route = "tambahJadwal",
                enterTransition = { powerPointPushEnter() },
                exitTransition = { powerPointPushExit() },
                popEnterTransition = { powerPointPushPopEnter() },
                popExitTransition = { powerPointPushPopExit() }
            ) {
                TambahJadwalScreen(
                    navController = navController,
                    viewModel = jadwalViewModel
                )
            }

            composable(
                route = "ubahJadwal/{jadwalId}",
                arguments = listOf(navArgument("jadwalId") { type = NavType.IntType }),
                enterTransition = { powerPointPushEnter() },
                exitTransition = { powerPointPushExit() },
                popEnterTransition = { powerPointPushPopEnter() },
                popExitTransition = { powerPointPushPopExit() }
            ) { backStackEntry ->
                val id = backStackEntry.arguments?.getInt("jadwalId") ?: return@composable
                UbahJadwalScreen(navController, jadwalViewModel, id)
            }

        }

    }
}
