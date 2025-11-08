package com.example.commitech.ui.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.IntOffset
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.example.commitech.ui.screen.*
import com.example.commitech.ui.screens.jadwal.UbahJadwalScreen
import com.example.commitech.ui.viewmodel.*

// Animation constants - PowerPoint style
private const val ANIMATION_DURATION = 500
private const val ANIMATION_DURATION_FAST = 350

// 🎬 PowerPoint Style: Push (Slide + Scale + Fade)
private fun powerPointPushEnter() = slideInHorizontally(
    initialOffsetX = { it },
    animationSpec = tween(ANIMATION_DURATION, easing = androidx.compose.animation.core.FastOutSlowInEasing)
) + scaleIn(
    initialScale = 0.95f,
    animationSpec = tween(ANIMATION_DURATION, easing = androidx.compose.animation.core.FastOutSlowInEasing)
) + fadeIn(
    animationSpec = tween(ANIMATION_DURATION, easing = androidx.compose.animation.core.FastOutSlowInEasing)
)

private fun powerPointPushExit() = slideOutHorizontally(
    targetOffsetX = { -it / 3 },
    animationSpec = tween(ANIMATION_DURATION, easing = androidx.compose.animation.core.FastOutSlowInEasing)
) + scaleOut(
    targetScale = 0.95f,
    animationSpec = tween(ANIMATION_DURATION, easing = androidx.compose.animation.core.FastOutSlowInEasing)
) + fadeOut(
    animationSpec = tween(ANIMATION_DURATION, easing = androidx.compose.animation.core.FastOutSlowInEasing)
)

private fun powerPointPushPopEnter() = slideInHorizontally(
    initialOffsetX = { -it / 3 },
    animationSpec = tween(ANIMATION_DURATION, easing = androidx.compose.animation.core.FastOutSlowInEasing)
) + scaleIn(
    initialScale = 0.95f,
    animationSpec = tween(ANIMATION_DURATION, easing = androidx.compose.animation.core.FastOutSlowInEasing)
) + fadeIn(
    animationSpec = tween(ANIMATION_DURATION, easing = androidx.compose.animation.core.FastOutSlowInEasing)
)

private fun powerPointPushPopExit() = slideOutHorizontally(
    targetOffsetX = { it },
    animationSpec = tween(ANIMATION_DURATION, easing = androidx.compose.animation.core.FastOutSlowInEasing)
) + scaleOut(
    targetScale = 0.95f,
    animationSpec = tween(ANIMATION_DURATION, easing = androidx.compose.animation.core.FastOutSlowInEasing)
) + fadeOut(
    animationSpec = tween(ANIMATION_DURATION, easing = androidx.compose.animation.core.FastOutSlowInEasing)
)

// 🎬 PowerPoint Style: Zoom (Scale + Fade)
private fun powerPointZoomEnter() = scaleIn(
    initialScale = 0.85f,
    animationSpec = tween(ANIMATION_DURATION, easing = androidx.compose.animation.core.FastOutSlowInEasing)
) + fadeIn(
    animationSpec = tween(ANIMATION_DURATION_FAST, easing = androidx.compose.animation.core.LinearOutSlowInEasing)
)

private fun powerPointZoomExit() = scaleOut(
    targetScale = 1.1f,
    animationSpec = tween(ANIMATION_DURATION, easing = androidx.compose.animation.core.FastOutSlowInEasing)
) + fadeOut(
    animationSpec = tween(ANIMATION_DURATION_FAST, easing = androidx.compose.animation.core.FastOutLinearInEasing)
)

// 🎬 PowerPoint Style: Fade Through (Crossfade)
private fun powerPointFadeEnter() = fadeIn(
    animationSpec = tween(ANIMATION_DURATION, easing = androidx.compose.animation.core.LinearOutSlowInEasing)
) + scaleIn(
    initialScale = 0.98f,
    animationSpec = tween(ANIMATION_DURATION, easing = androidx.compose.animation.core.FastOutSlowInEasing)
)

private fun powerPointFadeExit() = fadeOut(
    animationSpec = tween(ANIMATION_DURATION_FAST, easing = androidx.compose.animation.core.FastOutLinearInEasing)
) + scaleOut(
    targetScale = 1.02f,
    animationSpec = tween(ANIMATION_DURATION, easing = androidx.compose.animation.core.FastOutSlowInEasing)
)

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun AppNavGraph() {
    val navController = rememberNavController()
    
    // Shared ViewModels untuk sinkronisasi data
    val seleksiWawancaraViewModel: SeleksiWawancaraViewModel = viewModel()
    val pengumumanViewModel: PengumumanViewModel = viewModel()

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
                onSplashFinished = {
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
                onBackClick = { navController.popBackStack() },
                onLoginClick = { navController.navigate("login") },
                onSignUpClick = { navController.navigate("login") }
            )
        }

        // 🏡 Home
        composable(
            route = "home",
            enterTransition = { powerPointZoomEnter() },
            exitTransition = { powerPointZoomExit() }
        ) {
            HomeScreen(
                navController = navController,
                onDataPendaftarClick = { navController.navigate("dataPendaftar") },
                onSeleksiBerkasClick = { navController.navigate("seleksiBerkas") },
                onIsiJadwalClick = { navController.navigate("jadwal_graph") },
                onSeleksiWawancaraClick = { navController.navigate("seleksiWawancara") },
                onKelulusanClick = { navController.navigate("pengumumanKelulusan") },
                onAboutUsClick = { navController.navigate("aboutUs") }
            )
        }

        // ℹ️ About Us
        composable(
            route = "aboutUs",
            enterTransition = { powerPointPushEnter() },
            exitTransition = { powerPointPushExit() },
            popEnterTransition = { powerPointPushPopEnter() },
            popExitTransition = { powerPointPushPopExit() }
        ) {
            AboutUsScreen(
                onBackClick = { navController.popBackStack() },
                onHomeClick = { navController.popBackStack() }
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
            DataPendaftarScreen(viewModel = viewModel, onBackClick = { navController.popBackStack() })
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
            SeleksiBerkasScreen(viewModel = viewModel, onBackClick = { navController.popBackStack() })
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
                onBackClick = { navController.popBackStack() }
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
            ) { backStackEntry ->
                val parentEntry = remember(backStackEntry) {
                    navController.getBackStackEntry("jadwal_graph")
                }
                val sharedViewModel: JadwalViewModel = viewModel(parentEntry)
                JadwalRekrutmenScreen(
                    navController = navController,
                    viewModel = sharedViewModel
                )
            }

            composable(
                route = "tambahJadwal",
                enterTransition = { powerPointPushEnter() },
                exitTransition = { powerPointPushExit() },
                popEnterTransition = { powerPointPushPopEnter() },
                popExitTransition = { powerPointPushPopExit() }
            ) { backStackEntry ->
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
                arguments = listOf(navArgument("jadwalId") { type = NavType.IntType }),
                enterTransition = { powerPointPushEnter() },
                exitTransition = { powerPointPushExit() },
                popEnterTransition = { powerPointPushPopEnter() },
                popExitTransition = { powerPointPushPopExit() }
            ) { backStackEntry ->
                val parentEntry = remember(backStackEntry) {
                    navController.getBackStackEntry("jadwal_graph")
                }
                val sharedViewModel: JadwalViewModel = viewModel(parentEntry)
                val id = backStackEntry.arguments?.getInt("jadwalId") ?: return@composable
                UbahJadwalScreen(navController, sharedViewModel, id)
            }
            composable(
                route = "notifikasi",
                enterTransition = { powerPointPushEnter() },
                exitTransition = { powerPointPushExit() },
                popEnterTransition = { powerPointPushPopEnter() },
                popExitTransition = { powerPointPushPopExit() }
            ) {
                val jadwalViewModel: JadwalViewModel = viewModel()
                NotifikasiScreen(navController = navController, viewModel = jadwalViewModel)
            }

        }

    }
}
