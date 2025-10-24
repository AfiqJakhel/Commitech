package com.example.commitech.ui.navigation

sealed class Screen(val route: String) {
    object Landing : Screen("landing")
    object Login : Screen("login")
    object SignUp : Screen("signup")
    object DataPendaftar : Screen("dataPendaftar")
}