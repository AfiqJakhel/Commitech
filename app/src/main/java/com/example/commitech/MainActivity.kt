package com.example.commitech

import android.Manifest
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
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

/**
 * MainActivity - Entry point aplikasi Commitech
 *
 * CRITICAL FEATURES:
 * - Handle notification intent untuk deep linking
 * - Request notification permission (Android 13+)
 * - Setup notification channels
 * - Manage app theme (dark/light mode)
 *
 * NOTIFICATION HANDLING:
 * - onCreate: Handle saat app di-launch dari notifikasi (cold start)
 * - onNewIntent: Handle saat app sudah running dan user tap notifikasi
 */
class MainActivity : ComponentActivity() {

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) {
                InterviewNotificationHelper.ensureChannels(this)
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // ============================================================================
        // CRITICAL FIX #5: Log Notification Intent untuk Debugging
        // ============================================================================
        //
        // KENAPA PENTING?
        // - Membantu debug apakah intent dari notifikasi diterima dengan benar
        // - Memastikan data participant & schedule ada di intent
        // - Troubleshooting jika navigation tidak bekerja
        // ============================================================================
        
        logNotificationIntent(intent)

        maybeRequestNotificationPermission()
        InterviewNotificationHelper.ensureChannels(this)

        enableEdgeToEdge()
        setContent {
            val settingsViewModel: SettingsViewModel = viewModel()
            val settingsState by settingsViewModel.settingsState.collectAsState()

            CommitechTheme(darkTheme = settingsState.isDarkTheme) {
                // CRITICAL: AppNavGraph akan handle notification intent
                // dan navigate ke screen yang sesuai
                AppNavGraph(settingsViewModel = settingsViewModel)
            }
        }
    }

    // ============================================================================
    // CRITICAL FIX #6: Handle onNewIntent untuk App yang Sudah Running
    // ============================================================================
    //
    // MASALAH SEBELUMNYA:
    // - Saat app sudah running di background, tap notifikasi tidak trigger onCreate
    // - Android call onNewIntent dengan intent baru
    // - Intent tidak di-handle, navigation tidak terjadi
    //
    // SOLUSI:
    // - Override onNewIntent untuk handle intent baru
    // - Update current intent dengan setIntent()
    // - Log intent untuk debugging
    //
    // KENAPA PENTING?
    // - Saat user tap notifikasi dan app sudah running:
    //   - onCreate TIDAK dipanggil
    //   - onNewIntent dipanggil dengan intent baru
    //   - Kita harus handle ini untuk navigation yang benar
    //
    // FLOW:
    // 1. User tap notifikasi
    // 2. App sudah running di background
    // 3. Android call onNewIntent dengan intent dari notifikasi
    // 4. Kita update current intent
    // 5. Log intent untuk debugging
    // 6. NavGraph akan detect intent dan navigate
    //
    // NOTE: Untuk implementasi full navigation, perlu update NavGraph
    // untuk observe intent changes dan navigate accordingly
    // ============================================================================
    
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        
        // Update current intent agar NavGraph bisa access
        setIntent(intent)
        
        // Log untuk debugging
        logNotificationIntent(intent)
        
        // TODO: Implement navigation logic di NavGraph
        // Untuk sekarang, intent sudah di-set dan bisa diakses
        // Di future, bisa trigger navigation event atau recreate activity
    }

    /**
     * Log notification intent untuk debugging
     *
     * Fungsi ini membantu troubleshooting apakah:
     * - Intent dari notifikasi diterima
     * - Data participant & schedule ada
     * - Notification type benar
     */
    private fun logNotificationIntent(intent: Intent?) {
        if (intent == null) return
        
        val participantName = intent.getStringExtra(
            InterviewNotificationHelper.EXTRA_PARTICIPANT_NAME
        )
        val scheduleLabel = intent.getStringExtra(
            InterviewNotificationHelper.EXTRA_SCHEDULE_LABEL
        )
        val notificationType = intent.getStringExtra(
            InterviewNotificationHelper.EXTRA_NOTIFICATION_TYPE
        )
        
        if (participantName != null) {
            Log.d("MainActivity", "=== NOTIFICATION INTENT DETECTED ===")
            Log.d("MainActivity", "Participant: $participantName")
            Log.d("MainActivity", "Schedule: $scheduleLabel")
            Log.d("MainActivity", "Type: $notificationType")
            Log.d("MainActivity", "====================================")
            
            // TODO: Trigger navigation ke screen yang sesuai
            // Untuk sekarang, data sudah di-log dan bisa diakses
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
