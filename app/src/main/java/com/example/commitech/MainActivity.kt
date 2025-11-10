package com.example.commitech

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.commitech.notification.InterviewNotificationHelper
import com.example.commitech.ui.navigation.AppNavGraph
import com.example.commitech.ui.theme.CommitechTheme
import com.example.commitech.ui.viewmodel.SettingsViewModel

class MainActivity : ComponentActivity() {

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) {
                InterviewNotificationHelper.ensureChannels(this)
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        maybeRequestNotificationPermission()
        InterviewNotificationHelper.ensureChannels(this)

        enableEdgeToEdge()
        setContent {
            val settingsViewModel: SettingsViewModel = viewModel()
            val settingsState by settingsViewModel.settingsState.collectAsState()

            CommitechTheme(darkTheme = settingsState.isDarkTheme) {
                AppNavGraph(settingsViewModel = settingsViewModel)
            }
        }
    }

    private fun maybeRequestNotificationPermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return
        val permissionState = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.POST_NOTIFICATIONS
        )
        if (permissionState == android.content.pm.PackageManager.PERMISSION_GRANTED) {
            return
        }
        requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
    }
}
