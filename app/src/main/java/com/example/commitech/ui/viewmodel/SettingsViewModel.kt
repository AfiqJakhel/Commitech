package com.example.commitech.ui.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class SettingsState(
    val isDarkTheme: Boolean = false,
    val selectedLanguage: String = "Bahasa Indonesia",
    val notificationsEnabled: Boolean = true,
    val soundEnabled: Boolean = true,
    val autoBackup: Boolean = false
)

class SettingsViewModel : ViewModel() {
    private val _settingsState = MutableStateFlow(SettingsState())
    val settingsState: StateFlow<SettingsState> = _settingsState.asStateFlow()
    
    fun toggleDarkTheme(enabled: Boolean) {
        _settingsState.value = _settingsState.value.copy(isDarkTheme = enabled)
    }
    
    fun setLanguage(language: String) {
        _settingsState.value = _settingsState.value.copy(selectedLanguage = language)
    }
    
    fun toggleNotifications(enabled: Boolean) {
        _settingsState.value = _settingsState.value.copy(notificationsEnabled = enabled)
    }
    
    fun toggleSound(enabled: Boolean) {
        _settingsState.value = _settingsState.value.copy(soundEnabled = enabled)
    }
    
    fun toggleAutoBackup(enabled: Boolean) {
        _settingsState.value = _settingsState.value.copy(autoBackup = enabled)
    }
}
