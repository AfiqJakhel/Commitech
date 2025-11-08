package com.example.commitech

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.commitech.ui.navigation.AppNavGraph
import com.example.commitech.ui.theme.CommitechTheme
import com.example.commitech.ui.viewmodel.SettingsViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Enable edge-to-edge display (modern way)
        enableEdgeToEdge()
        setContent {
            val settingsViewModel: SettingsViewModel = viewModel()
            val settingsState by settingsViewModel.settingsState.collectAsState()
            
            CommitechTheme(darkTheme = settingsState.isDarkTheme) {
                AppNavGraph(settingsViewModel = settingsViewModel)
            }
        }
    }
}
