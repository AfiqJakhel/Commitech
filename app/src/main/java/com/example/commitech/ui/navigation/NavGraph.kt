package com.example.commitech.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.commitech.ui.screen.LandingScreen
import com.example.commitech.ui.screen.LoginScreen
import com.example.commitech.ui.screen.SplashScreen
import com.example.commitech.ui.screen.SignUpScreen

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
        composable(Screen.Landing.route) {
            LandingScreen(
                onLoginClick = { navController.navigate(Screen.Login.route) },
                onSignUpClick = { navController.navigate(Screen.SignUp.route) }
            )
        }

        // 🔐 Halaman Login
        composable(Screen.Login.route) {
            LoginScreen(
                onBackClick = { navController.popBackStack() },
                onLoginClick = { /* TODO: aksi login */ },
                onForgotPassword = { /* TODO */ },
                onSignUpClick = { navController.navigate(Screen.SignUp.route) }
            )
        }

        // 📝 Halaman SignUp (nanti kamu isi)
        composable(Screen.SignUp.route) {
            SignUpScreen(
                onBackClick = { navController.popBackStack() },
                onLoginClick = { navController.navigate(Screen.Login.route) },
                onSignUpClick = { /* TODO: aksi login */ },
            )
        }
    }
}
