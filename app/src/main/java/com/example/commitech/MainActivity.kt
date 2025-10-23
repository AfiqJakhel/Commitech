package com.example.commitech

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.Surface
import com.example.commitech.ui.screen.landing.LandingScreen
import com.example.commitech.ui.theme.CommitechTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CommitechTheme {
                // Surface mengikuti background dari tema (supaya konsisten light/dark)
                Surface {
                    LandingScreen(
                        onLoginClick = {
                            // TODO: Navigasi ke halaman Login
                            // Contoh nanti: navController.navigate("login")
                        },
                        onRegisterClick = {
                            // TODO: Navigasi ke halaman Register
                            // Contoh nanti: navController.navigate("register")
                        }
                    )
                }
            }
        }
    }
}
